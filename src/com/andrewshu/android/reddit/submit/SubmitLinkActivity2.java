package com.andrewshu.android.reddit.submit;

import java.util.regex.Pattern;

import org.apache.http.client.HttpClient;

import ssk.project.Practice.settings.RedditSettings;
import ssk.project.Practice.util.Util;
import android.app.TabActivity;
import android.os.Bundle;
import android.webkit.CookieSyncManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

import com.andrewshu.android.reddit.R;
import com.andrewshu.android.reddit.common.Constants;
import com.andrewshu.android.reddit.common.RedditIsFunHttpClientFactory;

public class SubmitLinkActivity2 extends TabActivity {

	public static final String TAG = "SubmitLinkActivity2";
	
	// Group 1: Subreddit. Group 2: thread id (no t3_prefix)
	private final Pattern NEW_THREAD_PATTERN = Pattern.compile(Constants.COMMENT_PATH_PATTERN_STRING);
	
	// Group1: whole error. Group 2: the time part
	private final Pattern RATELIMIT_RETRY_PATTERN = Pattern.compile("(you are trying to submit too fast. try again in (.+?)\\.)");
	//Group 1 Subreddit
	private final Pattern SUBMIT_PATH_PATTERN = Pattern.compile("/(?:r/([^/]+)/)?submit/?");
	
	TabHost mTabHost;
	
	private RedditSettings mSettings = new RedditSettings();
	private final HttpClient mClient = RedditIsFunHttpClientFactory.getGzipHttpClient();
	
	private String mSubmitUrl;
	
	private volatile String mCaptchaIden = null;
	private volatile String mCaptchaUrl = null;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		CookieSyncManager.createInstance(getApplicationContext());
		
		mSettings.loadRedditPreferences(this, mClient);
		setRequestedOrientation(mSettings.getRotation());
		setTheme(mSettings.getTheme());
		
		setContentView(R.layout.submit_link_main);
		
		final FrameLayout fl = (FrameLayout) findViewById(android.R.id.tabcontent);
		if (Util.isLightTheme(mSettings.getTheme())) {
			fl.setBackgroundResource(R.color.gray_75);
		} else {
			fl.setBackgroundResource(R.color.black);
		}
		
		mTabHost = getTabHost();
		mTabHost.addTab(mTabHost.newTabSpec(Constants.TAB_LINK).setIndicator("link").setContent(R.id.submit_link_view));
		mTabHost.addTab(mTabHost.newTabSpec(Constants.TAB_TEXT).setIndicator("text").setContent(R.id.submit_text_view));
		mTabHost.setOnTabChangedListener(new OnTabChangeListener() {
			public void onTabChanged(String tabId) {
				// Copy everything (except url and text) from old tab to new tab.
				final EditText submitLinkTitle = (EditText) findViewById(R.id.submit_link_title);
				final EditText submitLinkReddit = (EditText) findViewById(R.id.submit_link_reddit);
				final EditText submitTextTitle = (EditText) findViewById(R.id.submit_text_title);
				final EditText submitTextReddit = (EditText) findViewById(R.id.submit_text_reddit);
				if (Constants.TAB_LINK.equals(tabId)) {
					submitLinkTitle.setText(submitTextTitle.getText());
				}
			}
		});
		
	}
}
