package ssk.project.Practice.reddit.comments;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieSyncManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.andrewshu.android.reddit.R;
import com.andrewshu.android.reddit.comments.CommentsListActivity.CommentsListAdapter;
import com.andrewshu.android.reddit.common.CacheInfo;
import com.andrewshu.android.reddit.common.Common;
import com.andrewshu.android.reddit.common.Constants;
import com.andrewshu.android.reddit.common.RedditIsFunHttpClientFactory;
import com.andrewshu.android.reddit.common.tasks.HideTask;
import com.andrewshu.android.reddit.common.tasks.SaveTask;
import com.andrewshu.android.reddit.common.util.CollectionUtils;
import com.andrewshu.android.reddit.common.util.StringUtils;
import com.andrewshu.android.reddit.common.util.Util;
import com.andrewshu.android.reddit.login.LoginDialog;
import com.andrewshu.android.reddit.login.LoginTask;
import com.andrewshu.android.reddit.mail.InboxActivity;
import com.andrewshu.android.reddit.mail.PeekEnvelopeTask;
import com.andrewshu.android.reddit.markdown.MarkdownURL;
import com.andrewshu.android.reddit.settings.RedditPreferencesPage;
import com.andrewshu.android.reddit.settings.RedditSettings;
import com.andrewshu.android.reddit.things.ThingInfo;
import com.andrewshu.android.reddit.threads.ThreadsListActivity;
import com.andrewshu.android.reddit.threads.ThumbnailOnClickListenerFactory;
import com.andrewshu.android.reddit.user.ProfileActivity;

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
	
	
	
	
	
}
