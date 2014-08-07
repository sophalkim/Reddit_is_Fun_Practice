package ssk.project.Practice.reddit.comments;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;

import junit.framework.Assert;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;

import ssk.project.Practice.common.CacheInfo;
import ssk.project.Practice.common.Common;
import ssk.project.Practice.settings.RedditSettings;
import ssk.project.Practice.util.StringUtils;
import ssk.project.Practice.util.Util;
import android.os.AsyncTask;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.andrewshu.android.reddit.comments.CommentsListActivity;
import com.andrewshu.android.reddit.comments.ProcessCommentsTask.DeferredCommentProcessing;
import com.andrewshu.android.reddit.common.Constants;
import com.andrewshu.android.reddit.common.ProgressInputStream;
import com.andrewshu.android.reddit.markdown.Markdown;
import com.andrewshu.android.reddit.things.Listing;
import com.andrewshu.android.reddit.things.ListingData;
import com.andrewshu.android.reddit.things.ThingInfo;
import com.andrewshu.android.reddit.things.ThingListing;
import com.andrewshu.android.reddit.threads.ShowThumbnailsTask;
import com.andrewshu.android.reddit.threads.ShowThumbnailsTask.ThumbnailLoadAction;

public class DownloadCommentsTask extends AsyncTask<Integer, Long, Boolean> implements PropertyChangeListener {

	private static final String TAG = "CommentsListActivity.DownloadCommentsTask";
	private final ObjectMapper mObjectMapper = Common.getObjectMapper();
	private final Markdown markdown = new Markdown();
	
	private static AsyncTask<?, ?, ?> mCurrentDownloadCommentsTask = null;
	private static final Object mCurrentDownloadCommentsTaskLock = new Object();
	
	private ShowThumbnailsTask mCurrentShowThumbnailsTask = null;
	private final Object mCurrentShowThumbnailsTaskLock = new Object();
	
	private ProcessCommentsTask mProcessCommentsTask;
	
	private CommentsListActivity mActivity;
	private String mSubreddit;
	private String mThreadId;
	private String mThreadTitle;
	private RedditSettings mSettings;
	private HttpClient mClient;
	
	private int mPositionOffset = 0;
	private int mIndentation = 0;
	private String mMoreChildrenId = "";
	private ThingInfo mOpThingInfo = null;
	
	private long mContentLength = 0;
	
	private String mJumpToCommentId = "";
	private int mJumpToCommentFoundIndex = -1;
	
	private int mJumpToCommentContext = 0;
	
	private final LinkedList<ThingInfo> mDeferredAppendList = new LinkedList<ThingInfo>();
	private final LinkedList<ThingInfo> mDeferredReplacementList = new LinkedList<ThingInfo>();
	
	public DownloadCommentsTask(CommentsListActivity activity, String subreddit, String threadId, RedditSettings settings, HttpClient client) {
		mActivity = activity;
		mSubreddit = subreddit;
		mThreadId = threadId;
		mSettings = settings;
		mClient = client;
		mProcessCommentsTask = new ProcessCommentsTask(activity);
	}
	
	public DownloadCommentsTask prepareLoadMoreComments(String moreChildrenId, int morePosition, int indentation) {
		mMoreChildrenId = moreChildrenId;
		mPositionOffset = morePosition;
		mIndentation = indentation;
		return this;
	}
	
	public DownloadCommentsTask prepareLoadAndJumpToComment(String commentId, int context) {
		mJumpToCommentId = commentId;
		mJumpToCommentContext = context;
		return this;
	}
	
	@Override
	protected Boolean doInBackground(Integer... maxComments) {
		HttpEntity entity = null;
		try {
			StringBuilder sb = new StringBuilder(Constants.REDDIT_BASE_URL);
			if (mSubreddit != null) {
				sb.append("/r/").append(mSubreddit.trim());
			}
			sb.append("/comments/")
				.append(mThreadId)
				.append("/z/").append(mMoreChildrenId).append("/.json?")
				.append(mSettings.getCommentsSortByUrl()).append("&");
			if (mJumpToCommentContext != 0) {
				sb.append("context=").append(mJumpToCommentContext).append("&");
			}
			String url = sb.toString();
			InputStream in = null;
			boolean currentlyUsingCache = false;
			
			if (Constants.USE_COMMENTS_CACHE) {
				try {
					if (CacheInfo.checkFreshThreadCache(mActivity.getApplicationContext()) &&
							url.equals(CacheInfo.getCachedThreadUrl(mActivity.getApplicationContext()))) {
						in = mActivity.openFileInput(Constants.FILENAME_THREAD_CACHE);
						mContentLength = mActivity.getFileStreamPath(Constants.FILENAME_THREAD_CACHE).length();
						currentlyUsingCache = true;
						if (Constants.LOGGING) Log.d(TAG, "Using cached thread JSON, length=" + mContentLength);
					}
				} catch (Exception cacheEx) {
					if (Constants.LOGGING) Log.d(TAG, "skip cache", cacheEx);
				}
			}
			
			if (!currentlyUsingCache) {
				HttpGet request = new HttpGet(url);
				HttpResponse response = mClient.execute(request);
				
				Header contentLengthHeader = response.getFirstHeader("Content-Length");
				if (contentLengthHeader != null) {
					mContentLength = Long.valueOf(contentLengthHeader.getValue());
					if (Constants.LOGGING) Log.d(TAG, "Content Length: " + mContentLength);
				} else {
					mContentLength = -1;
					if (Constants.LOGGING) Log.d(TAG, "Content Length: UNAVAILABLE");
				}
				
				entity = response.getEntity();
				in = entity.getContent();
				
				if (Constants.USE_COMMENTS_CACHE) {
					in = CacheInfo.writeThenRead(mActivity.getApplicationContext(), in, Constants.FILENAME_THREAD_CACHE);
					try {
						CacheInfo.setCachedThreadUrl(mActivity.getApplicationContext(), url);
					} catch (IOException e) {
						
					}
				}
			}
			
			ProgressInputStream pin = new ProgressInputStream(in, mContentLength);
			pin.addPropertyChangeListener(this);
			
			parseCommentsJSON(pin);
			if (Constants.LOGGING) Log.d(TAG, "parseCommentsJSON completed");
			
			pin.close();
			in.close();
			
			return true;
			
		} catch (Exception e) {
			if (Constants.LOGGING) Log.e(TAG, "DownloadCommentTask", e);
		} finally {
			if (entity != null) {
				try {
					entity.consumeContent();
				} catch (Exception e2) {
					if (Constants.LOGGING) Log.e(TAG, "entity.consumeContent()", e2);
				}
			}
		}
		return false;
	}
	
	private void replaceCommentsAtPositionUI(final Collection<ThingInfo> comments, final int position) {
		mActivity.mCommentsList.remove(position);
		mActivity.mCommentsList.addAll(position, comments);
		mActivity.mCommentsAdapter.notifyDataSetChanged();
	}

	private void deferCommentAppend(ThingInfo comment) {
		mDeferredAppendList.add(comment);
	}
	
	private void deferCommentReplacement(ThingInfo comment) {
		mDeferredReplacementList.add(comment);
	}
	
	private boolean isInsertingEntireThread() {
		return mPositionOffset == 0;
	}
	
	private void disableLoadingScreenKeepProgress() {
		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mActivity.resetUI(mActivity.mCommentsAdapter);
			}
		});
	}
	
	private void parseCommentsJSON(InputStream in) throws IOException, JsonParseException {
		int insertedCommentIndex;
		String genericListingError = "Not a comments listing";
		try {
			Listing[] listings = mObjectMapper.readValue(in, Listing[].class);
			Assert.assertEquals(Constants.JSON_LISTING, listings[0].getKind(), genericListingError);
			
			ListingData threadListingData = listings[0].getData();
			if (StringUtils.isEmpty(threadListingData.getModhash())) {
				mSettings.setModhash(null);
			} else {
				mSettings.setModhash(threadListingData.getModhash());
			}
			
			if (Constants.LOGGING) Log.d(TAG, "Successfully get OP listing[0]: modhash" + mSettings.getModhash());
			
			ThingListing threadThingListing = threadListingData.getChildren()[0];
			Assert.assertEquals(Constants.THREAD_KIND, threadThingListing.getKind(), genericListingError);
			
			if (isInsertingEntireThread()) {
				parseOP(threadThingListing.getData());
				insertedCommentIndex = 0;
				disableLoadingScreenKeepProgress();
			} else {
				insertedCommentIndex = mPositionOffset - 1;
			}
			
			ListingData commentListingData = listings[1].getData();
			for (ThingListing commentThingListing : commentListingData.getChildren()) {
				insertedCommentIndex = insertNestedComment(commentThingListing, 0, insertedCommentIndex + 1);
			}
			
			mProcessCommentsTask.mergeLowPriorityListToMainList();
		} catch (Exception ex) {
			if (Constants.LOGGING) Log.e(TAG, "parseCommentsJSON", ex);
		}
	}
	
	private void parseOP(final ThingInfo data) {
		data.setIndent(0);
		data.setClicked(Common.isClicked(mActivity, data.getUrl()));
		
		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mActivity.mCommentsList.add(0, data);
			}
		});
		
		if (data.isIs_self() && data.getSelftext_html() != null) {
			String unescapedHtmlSelftext = Html.fromHtml(data.getSelftext_html()).toString();
			Spanned selftext = Html.fromHtml(Util.convertHtmlTags(unescapedHtmlSelftext));
			
			if (selftext.length() > 2) {
				data.setSpannedSelftext(selftext.subSequence(0, selftext.length() - 2));
			} else {
				data.setSpannedSelftext("");
			}
			markdown.getURLs(data.getSelftext(), data.getUrls());
		}
		
		mThreadTitle = data.getTitle();
		mActivity.setThreadTitle(mThreadTitle);
		mSubreddit = data.getSubreddit();
		mThreadId = data.getId();
		
		mOpThingInfo = data;
	}
	
	int insertNestedComment(ThingListing commentThingListing, int indentLevel, int insertedCommentIndex) {
		ThingInfo ci = commentThingListing.getData();
		
		if (isInsertingEntireThread()) {
			deferCommentAppend(ci);
		} else {
			deferCommentReplacement(ci);
		}
		
		if (isHasJumpTarget()) {
			if (!isFoundJumpTargetComment() && mJumpToCommentId.equals(ci.getId())) {
				processJumpTarget(ci, insertedCommentIndex);
			}
		}
		
		if (isHasJumpTarget()) {
			if (isFoundJumpTargetComment()) {
				mProcessCommentsTask.addDeferred(new DeferredCommentProcessing(ci, insertedCommentIndex));
			} else {
				if (mJumpToCommentContext > 0) {
					mProcessCommentsTask.addDeferredHighPriority(new DeferredCommentProcessing(ci, insertedCommentIndex));
				} else {
					mProcessCommentsTask.addDeferredLowPriority(new DeferredCommentProcessing(ci, insertedCommentIndex));
				}
			}
		} else {
			mProcessCommentsTask.addDeferred(new DeferredCommentProcessing(ci, insertedCommentIndex));
		}
		
		ci.setIndent(mIndentation + indentLevel);
		
		if (Constants.MORE_KIND.equals(commentThingListing.getKind())) {
			ci.setLoadMoreCommentsPlaceholder(true);
			if (Constants.LOGGING) Log.v(TAG, "new more position at " + (insertedCommentIndex));
			return insertedCommentIndex;
		}
		
		if (!Constants.COMMENT_KIND.equals(commentThingListing.getKind())) {
			if (Constants.LOGGING) Log.e(TAG, "comment whose kind is \"" + commentThingListing.getKind() + "\" (expected " + Constants.COMMENT_KIND + ")");
			return insertedCommentIndex;
		}
		
		Listing repliesListing = ci.getReplies();
		if (repliesListing == null)
			return insertedCommentIndex;
		ListingData repliesListingData = repliesListing.getData();
		if (repliesListingData == null)
			return insertedCommentIndex;
		ThingListing[] replyThingListings = repliesListingData.getChildren();
		if (replyThingListings == null)
			return insertedCommentIndex;
		for (ThingListing replyThingListing : replyThingListings) {
			insertedCommentIndex = insertNestedComment(replyThingListing, indentLevel + 1, insertedCommentIndex + 1);
		}
		return insertedCommentIndex;
	}
	
	private boolean isHasJumpTarget() {
		return !StringUtils.isEmpty(mJumpToCommentId);
	}
	
	private boolean isFoundJumpTargetComment() {
		return mJumpToCommentFoundIndex != -1;
	}
	
	private void processJumpTarget(ThingInfo comment, int commentIndex) {
		mJumpToCommentFoundIndex = (commentIndex - mJumpToCommentContext) > 0 ? (commentIndex - mJumpToCommentContext) : 0;
		mProcessCommentsTask.mergeHighPriorityListToMainList();
	}
	
	private void insertCommentsUI() {
		mActivity.mCommentsList.addAll(mDeferredAppendList);
		mActivity.mCommentsAdapter.notifyDataSetChanged();
	}
	
	private void processDeferredComments() {
		mProcessCommentsTask.execute();
	}
	
	private void showOPThumbnail() {
		if (mOpThingInfo != null) {
			synchronized (mCurrentShowThumbnailsTaskLock) {
				if (mCurrentShowThumbnailsTask != null)
					mCurrentShowThumbnailsTask.cancel(true);
				mCurrentShowThumbnailsTask = new ShowThumbnailsTask(mActivity, mClient, null);
			}
			mCurrentShowThumbnailsTask.execute(new ThumbnailLoadAction(mOpThingInfo, null, 0));
		}
	}
	
	void cleanupDeferred() {
		mDeferredAppendList.clear();
		mDeferredReplacementList.clear();
	}
	
	@Override
	public void onPreExecute() {
		if (mThreadId == null) {
			if (Constants.LOGGING) Log.e(TAG, "mSettings.threadId == null");
			cancel(true);
			return;
		}
		synchronized (mCurrentDownloadCommentsTaskLock) {
			if (mCurrentDownloadCommentsTask != null) {
				this.cancel(true);
				return;
			}
			mCurrentDownloadCommentsTask = this;
		}
		
		if (isInsertingEntireThread()) {
			if (mActivity.mCommentsAdapter != null)
				mActivity.mCommentsAdapter.clear();
			else
				mActivity.resetUI(null);
			mActivity.enableLoadingScreen();
		}
		
		if (mContentLength == -1)
			mActivity.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_INDETERMINATE_ON);
		if (mThreadTitle != null)
			mActivity.setTitle(mThreadTitle + " : " + mSubreddit);
	}
	
	@Override
	public void onPostExecute(Boolean success) {
		if (isInsertingEntireThread()) {
			insertCommentsUI();
			if (isFoundJumpTargetComment())
				mActivity.getListView().setSelection(mJumpToCommentFoundIndex);
		} else if (!mDeferredReplacementList.isEmpty()) {
			replaceCommentsAtPositionUI(mDeferredReplacementList, mPositionOffset);
		}
		processDeferredComments();
		if (Common.shouldLoadThumbnails(mActivity, mSettings));
			showOPThumbnail();
		mActivity.markSubmitterComments();
		
		if (mContentLength = -1)
			mActivity.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_INDETERMINATE_OFF);
		else 
			mActivity.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_END);
		
		if (success) {
			mActivity.setShouldClearReply(true);
			if (mThreadTitle != null)
				mActivity.setTitle(mThreadTitle + " : " + mSubreddit);
		} else {
			if (!isCancelled()) {
				Common.showErrorToast("Error downloading comments. Please try again.", Toast.LENGTH_LONG, mActivity);
				mActivity.resetUI(null);
			}
		}
		
		synchronized (mCurrentDownloadCommentsTaskLock) {
			mCurrentDownloadCommentsTask = null;
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		
	}

	
}
