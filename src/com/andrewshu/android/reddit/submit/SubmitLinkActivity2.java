package com.andrewshu.android.reddit.submit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.HttpClient;

import ssk.project.Practice.settings.RedditSettings;
import ssk.project.Practice.util.StringUtils;
import ssk.project.Practice.util.Util;
import android.app.TabActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieSyncManager;
import android.widget.Button;
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
					submitLinkReddit.setText(submitTextReddit.getText());
				} else {
					submitTextTitle.setText(submitLinkTitle.getText());
					submitTextReddit.setText(submitLinkReddit.getText());
				}
			}
		});
		mTabHost.setCurrentTab(0);
		
		if (mSettings.isLoggedIn()) {
			start();
		} else {
			showDialog(Constants.DIALOG_LOGIN);
		}
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mSettings.saveRedditPreferences(this);
		CookieSyncManager.getInstance().stopSync();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mSettings.saveRedditPreferences(this);
		CookieSyncManager.getInstance().stopSync();
	}
	
	
	/**
	 * Enable the UI after user is logged in.
	 */
	public void start() {
		// Intents can be external (browser share page) or from Reddit is fun.
		String intentAction = getIntent().getAction();
		if (Intent.ACTION_SEND.equals(intentAction)) {
			// Share
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				String url = extras.getString(Intent.EXTRA_TEXT);
				final EditText submitLinkUrl = (EditText) findViewById(R.id.submit_link_url);
				final EditText submitLinkReddit = (EditText) findViewById(R.id.submit_link_reddit);
				final EditText submitTextReddit = (EditText) findViewById(R.id.submit_text_reddit);
				submitLinkUrl.setText(url);
				submitLinkReddit.setText("");
				submitTextReddit.setText("");
				mSubmitUrl = Constants.REDDIT_BASE_URL + "/submit";
			}
		} else {
			String submitPath = null;
			Uri data = getIntent().getData();
			if (data != null && Util.isRedditUri(data))
				submitPath = data.getPath();
			if (submitPath == null)
				submitPath = "/submit";
			
			// the URL to do HTTP POST to
			mSubmitUrl = Util.absolutePathToURL(submitPath);
			
			// Put the subreddit in the text field
			final EditText submitLinkReddit = (EditText) findViewById(R.id.submit_link_reddit);
			final EditText submitTextReddit = (EditText) findViewById(R.id.submit_text_reddit);
			Matcher m = SUBMIT_PATH_PATTERN.matcher(submitPath);
			if (m.matches()) {
				String subreddit = m.group(1);
				if (StringUtils.isEmpty(subreddit)) {
					submitLinkReddit.setText("");
					submitTextReddit.setText("");
				} else {
					submitLinkReddit.setText(subreddit);
					submitTextReddit.setText(subreddit);
				}
			}
		}
		
		final Button submitLinkButton = (Button) findViewById(R.id.submit_link_button);
		submitLinkButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (validateLinkForm()) {
					final EditText submitLinkTitle = (EditText) findViewById(R.id.submit_link_title);
					final EditText submitLinkUrl = (EditText) findViewById(R.id.submit_link_url);
					final EditText submitLinkReddit = (EditText) findViewById(R.id.submit_link_redi);
					final EditText submitLinkCaptcha = (EditText) findViewByid(R.id.submit_link_captcha);
					new SubmitLinkTask(submitLinkTitle.getText().toString(), 
							submitLinkUrl.getText().toString(),
							submitLinkReddit.getText().toString(),
							Constants.SUBMIT_KIND_LINK,
							submitLinkCaptcha.getText().toString()).execute();
							
				}
			}
		});
		final Button submitTextButton = (Button) findViewById(R.id.submit_text_button);
		submitTextButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (validateTextForm()) {
					final EditText submitTextTitle = (EditText) findViewById(R.id.submit_text_title);
					final EditText submitTextText = (EditText) findViewById(R.id.submit_text_text);
					final EditText submitTextReddit = (EditText) findViewById(R.id.submit_text_reddit);
					final EditText submitTextCaptcha = (EditText) findViewById(R.id.submit_text_captcha);
					new SubmitLinkTask(submitTextTitle.getText().toString(),
							submitTextText.getText().toString(),
							submitTextReddit.getText().toString(),
							Constants.SUBMIT_KIND_SELF,
							submitTextCaptcha.getText().toString()).execute();
				}
			}
		});
		// Check the CAPTCHA
		new MyCaptchaCheckRequiredTask().execute();
	}
	
	
}
