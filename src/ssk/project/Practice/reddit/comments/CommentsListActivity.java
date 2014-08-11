package ssk.project.Practice.reddit.comments;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.http.client.HttpClient;

import ssk.project.Practice.settings.RedditSettings;
import ssk.project.Practice.things.ThingInfo;
import android.app.Activity;
import android.app.ListActivity;
import android.view.View;

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
	
}
