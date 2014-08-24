package ssk.project.Practice.reddit.comments;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.HttpClient;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieSyncManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.andrewshu.android.reddit.R;
import com.andrewshu.android.reddit.common.Constants;
import com.andrewshu.android.reddit.common.RedditIsFunHttpClientFactory;
import com.andrewshu.android.reddit.common.util.StringUtils;
import com.andrewshu.android.reddit.common.util.Util;
import com.andrewshu.android.reddit.mail.PeekEnvelopeTask;
import com.andrewshu.android.reddit.settings.RedditSettings;
import com.andrewshu.android.reddit.things.ThingInfo;
import com.andrewshu.android.reddit.threads.ThreadsListActivity;

public class CommentsListActivity extends ListActivity 
		implements View.OnCreateContextMenuListener {

	private static final String TAG = "CommentsListActivity";
	
	private final Pattern COMMENT_PATH_PATTERN = Pattern.compile(Constants.COMMENT_PATH_PATTERN_STRING);
	private final Pattern COMMENT_CONTEXT_PATTERN = Pattern.compile("context=(\\d+)");
	
	public CommentsListAdapter mCommentsAdapter = null;
	public ArrayList<ThingInfo> mCommentsList = null;
	
	private final HttpClient mClient = RedditIsFunHttpClientFactory.getGzipHttpClient();
	private final RedditSettings mSettings = new RedditSettings();
	
	private String mSubreddit = null;
	private String mThreadId = null;
	private String mThreadTitle = null;
	
	private ThingInfo mVoteTargetThing = null;
	private String mReportTargetName = null;
	private String mReplyTargetName = null;
	private String mEditTargetBody = null;
	private String mDeleteTargetKind = null;
	private boolean mShouldClearReply = false;
	
	private String last_search_string;
	private int last_found_position = -1;
	
	private boolean mCanChord = false;
	
	public static Method mActivity_overridePendingTransition;
	
	static {
		initCompatibility();
	}
	
	private static void initCompatibility() {
		try {
			mActivity_overridePendingTransition = Activity.class.getMethod(
					"overridePendingTransition", new Class[] { Integer.TYPE, Integer.TYPE });
		} catch (NoSuchMethodException nsme) {
			
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		CookieSyncManager.createInstance(getApplicationContext());
		
		mSettings.loadRedditPreferences(this, mClient);
		
		setRequestedOrientation(mSettings.getRotation());
		setTheme(mSettings.getTheme());
		requestWindowFeature(Window.FEATURE_PROGRESS);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		setContentView(R.layout.comments_list_content);
		registerForContextMenu(getListView());
		
		if (savedInstanceState != null) {
			mReplyTargetName = savedInstanceState.getString(Constants.REPLY_TARGET_NAME_KEY);
			mReportTargetName = savedInstanceState.getString(Constants.REPORT_TARGET_NAME_KEY);
			mEditTargetBody = savedInstanceState.getString(Constants.EDIT_TARGET_BODY_KEY);
			mDeleteTargetKind = savedInstanceState.getString(Constants.DELETE_TARGET_KIND_KEY);
			mThreadTitle = savedInstanceState.getString(Constants.THREAD_ID_KEY);
			mSubreddit = savedInstanceState.getString(Constants.SUBREDDIT_KEY);
			mThreadId = savedInstanceState.getString(Constants.THREAD_ID_KEY);
			mVoteTargetThing = savedInstanceState.getParcelable(Constants.VOTE_TARGET_THING_INFO_KEY);
		
			if (mThreadTitle != null) {
				setTitle(mThreadTitle + " : " + mSubreddit);
			}
			
			mCommentsList = (ArrayList<ThingInfo>) getLastNonConfigurationInstance();
			if (mCommentsList == null) {
				getNewDownloadCommentsTask().execute(Constants.DEFAULT_COMMENT_DOWNLOAD_LIMIT);
			} else {
				resetUI(new CommentsListAdapter(this, mCommentsList));
			}
		} else {
			String commentPath;
			String commentQuery;
			String jumpToCommentId = null;
			int jumpToCommentContext = 0;
			Uri data = getIntent().getData();
			if (data != null) {
				commentPath = data.getPath();
				commentQuery = data.getQuery();
			} else {
				if (Constants.LOGGING) Log.e(TAG, "Quitting because no subreddit and thread id data was passed into the Intent");
				finish();
				return;
			}
			if (commentPath != null) {
				if (Constants.LOGGING) Log.d(TAG, "comment path: " + commentPath);
				if (Util.isRedditShortenedUri(data)) {
					mThreadId = commentPath.substring(1);
				} else {
					Matcher m = COMMENT_PATH_PATTERN.matcher(commentPath);
					if (m.matches()) {
						mSubreddit = m.group(1);
						mThreadId = m.group(2);
						jumpToCommentId = m.group(3);
					}
				}
			} else {
				if (Constants.LOGGING) Log.e(TAG, "Quitting because of bad comment path.");
				finish();
				return;
			}
			if (commentQuery != null) {
				Matcher m = COMMENT_CONTEXT_PATTERN.matcher(commentQuery);
				if (m.find()) {
					jumpToCommentContext = m.group(1) != null ? Integer.valueOf(m.group(1)) : 0;
				}
			}
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				String subreddit = extras.getString(Constants.EXTRA_SUBREDDIT);
				if (subreddit != null) {
					mSubreddit = subreddit;
				}
				mThreadTitle = extras.getString(Constants.EXTRA_TITLE);
				if (mThreadTitle != null) {
					setTitle(mThreadTitle + " : " + mSubreddit);
				}
			}
			
			if (!StringUtils.isEmpty(jumpToCommentId)) {
				getNewDownloadCommentsTask().prepareLoadAndJumpToComment(jumpToCommentId, jumpToCommentContext)
						.execute(Constants.DEFAULT_COMMENT_DOWNLOAD_LIMIT);
			} else {
				getNewDownloadCommentsTask().execute(Constants.DEFAULT_COMMENT_DOWNLOAD_LIMIT);
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		int previousTheme = mSettings.getTheme();
		
		mSettings.loadRedditPreferences(this, mClient);
		if (mSettings.getTheme() != previousTheme) {
			relaunchActivity();
		} else {
			CookieSyncManager.getInstance().startSync();
			setRequestedOrientation(mSettings.getRotation());
			
			if (mSettings.isLoggedIn()) {
				new PeekEnvelopeTask(this, mClient, mSettings.getMailNotificationStyle()).execute();
			}
		}
	}
	
	private void relaunchActivity() {
		finish();
		startActivity(getIntent());
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		CookieSyncManager.getInstance().stopSync();
		mSettings.saveRedditPreferences(this);
	}
	
	private DownloadCommentsTask getNewDownloadCommentsTask() {
		return new DownloadCommentsTask(this, mSubreddit, mThreadId, mSettings, mClient);
	}
	
	private boolean isHiddenCommentHeadPosition(int position) {
		return mCommentsAdapter != null && mCommentsAdapter.getItemViewType(position) == CommentsListAdapter.HIDDEN_ITEM_HEAD_VIEW_TYPE;
	}
	
	private boolean isHiddenCommentDescendantPosition(int position) {
		return mCommentsAdapter != null && mCommentsAdapter.getItem(position).isHiddenCommentHead();
	}
	
	private boolean isLoadMoreCommentsPosition(int position) {
		return mCommentsAdapter != null && mCommentsAdapter.getItemViewType(position) == CommentsListAdapter.MORE_ITEM_VIEW_TYPE;
	}
	
	public final class CommentsListAdapter extends ArrayAdapter<ThingInfo> {
		public static final int OP_ITEM_VIEW_TYPE = 0;
		public static final int COMMENT_ITEM_VIEW_TYPE = 1;
		public static final int MORE_ITEM_VIEW_TYPE = 2;
		public static final int HIDDEN_ITEM_HEAD_VIEW_TYPE = 3;
		public static final int VIEW_TYPE_COUNT = 4;
		
		public boolean mIsLoading = true;
		
		private LayoutInflater mInflater;
		private int mFrequentSeparatorPos = ListView.INVALID_POSITION;
		
		public CommentsListAdapter(Context context, List<ThingInfo> objects) {
			super(context, 0, objects);
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public int getItemViewType(int position) {
			if (position == 0) {
				return OP_ITEM_VIEW_TYPE;
			}
			if (position == mFrequentSeparatorPos) {
				return IGNORE_ITEM_VIEW_TYPE;
			}
			
			ThingInfo item = getItem(position);
			if (item.isHiddenCommentDescendant())
				return IGNORE_ITEM_VIEW_TYPE;
			if (item.isHiddenCommentHead())
				return HIDDEN_ITEM_HEAD_VIEW_TYPE;
			if (item.isLoadMoreCommentsPlaceholder())
				return MORE_ITEM_VIEW_TYPE;
			
			return COMMENT_ITEM_VIEW_TYPE;
		}
		
		@Override
		public int getViewTypeCount() {
			return VIEW_TYPE_COUNT;
		}
		
		@Override
		public boolean isEmpty() {
			if (mIsLoading)
				return false;
			return super.isEmpty();
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			ThingInfo item = this.getItem(position);
			
			try {
				if (position == 0) {
					if (view == null) {
						view = mInflater.inflate(R.layout.threads_list_item, null);
					}
					
					ThreadsListActivity.fillThreadsListItemView(
							position, view, item, CommentsListActivity.this, mClient, mSettings, mThumbnailOnClickListenerFactory);
				}
				if (item.isIs_self()) {
					View thumbnailContainer = view.findViewById(R.id.thumbnail_view);
					if (thumbnailContainer != null)
						thumbnailContainer.setVisibility(View.GONE);
				}
				
				TextView submissionStuffView = (TextView) view.findViewById(R.id.submissionTime_submitter);
				TextView selftextView = (TextView) view.findViewById(R.id.selftext);
				
				submissionStuffView.setVisibility(View.VISIBLE);
				submissionStuffView.setText(String.format(getResources().getString(R.string.thread_time_submitter), Util.getTimeAgo(item.getCreated_utc()), item.getAuthor()));
				
				if (!StringUtils.isEmpty(item.getSpannedSelftext())) {
					selftextView.setVisibility(View.VISIBLE);
					selftextView.setText(item.getSpannedSelftext());
				} else {
					selftextView.setVisibility(View.GONE);
				}
			}
		}
		
		
	}
	
	
	
}
