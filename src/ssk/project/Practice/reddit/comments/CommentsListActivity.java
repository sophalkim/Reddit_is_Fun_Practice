package ssk.project.Practice.reddit.comments;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.http.client.HttpClient;

import ssk.project.Practice.settings.RedditSettings;
import ssk.project.Practice.things.ThingInfo;
import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.CookieSyncManager;

import com.andrewshu.android.reddit.R;
import com.andrewshu.android.reddit.comments.CommentsListActivity.CommentsListAdapter;
import com.andrewshu.android.reddit.common.Constants;
import com.andrewshu.android.reddit.common.RedditIsFunHttpClientFactory;

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
			if (Constants.LOGGING) Log.e(TAG, "Quitting because no subreddit and thread id data was passed into the Intent");
		}
	}
	
}
