package ssk.project.Practice.reddit.comments;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.codehaus.jackson.map.ObjectMapper;

import ssk.project.Practice.common.CacheInfo;
import ssk.project.Practice.common.Common;
import ssk.project.Practice.settings.RedditSettings;
import android.os.AsyncTask;
import android.util.Log;

import com.andrewshu.android.reddit.comments.CommentsListActivity;
import com.andrewshu.android.reddit.common.Constants;
import com.andrewshu.android.reddit.common.ProgressInputStream;
import com.andrewshu.android.reddit.markdown.Markdown;
import com.andrewshu.android.reddit.things.ThingInfo;
import com.andrewshu.android.reddit.threads.ShowThumbnailsTask;

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
	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		
	}

	
}
