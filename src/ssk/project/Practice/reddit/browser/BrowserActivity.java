package ssk.project.Practice.reddit.browser;

import java.lang.reflect.Method;

import ssk.project.Practice.common.Common;
import ssk.project.Practice.settings.RedditSettings;
import ssk.project.Practice.util.Util;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.andrewshu.android.reddit.R;
import com.andrewshu.android.reddit.comments.CommentsListActivity;
import com.andrewshu.android.reddit.common.Constants;

public class BrowserActivity extends Activity {

	private static final String TAG = "BrowserActivity";
	
	private WebView webview;
	private Uri mUri = null;
	private String mThreadUrl = null;
	private String mTitle = null;
	
	private final RedditSettings mSettings = new RedditSettings();
	
	private static Method mWebSettings_setDomStorageEnabled;
	private static Method mWebSettings_setLoadWithOverviewMode;
	
	static {
		initCompatibility();
	}
	
	private static void initCompatibility() {
		try {
			mWebSettings_setDomStorageEnabled = WebSettings.class.getMethod("setDomStorageEnabled", new Class[] { Boolean.TYPE });
		} catch (NoSuchMethodException nsme) {
			
		}
		try {
			mWebSettings_setLoadWithOverviewMode = WebSettings.class.getMethod("setLoadWithOverviewMode", new Class[] { Boolean.TYPE });
		} catch (NoSuchMethodException nsme) {
			
		}
	}
	
	private void trySetDomStorageEnabled(WebSettings settings) {
		if (mWebSettings_setDomStorageEnabled != null) {
			try {
				mWebSettings_setDomStorageEnabled.invoke(settings, true);
			} catch (Exception ex) {
				Log.e(TAG, "trySetDomStorageEnabled", ex);
			}
		}
	}
	
	private void trySetLoadWithOverviewMode(WebSettings settings) {
		if (mWebSettings_setLoadWithOverviewMode != null) {
			try {
				mWebSettings_setLoadWithOverviewMode.invoke(settings, true);
				return;
			} catch (Exception ex){
				Log.e(TAG, "trySetLoadWithOverviewMode", ex);
			}
		}
		webview.setInitialScale(50);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		CookieSyncManager.getInstance().startSync();
		mSettings.loadRedditPreferences(this, null);
		setRequestedOrientation(mSettings.getRotation());
		int previousTheme = mSettings.getTheme();
		if (mSettings.getTheme() != previousTheme) {
			resetUI();
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		CookieSyncManager.getInstance().stopSync();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		webview.setVisibility(View.GONE);
		webview.destroy();
		webview = null;
	}
	
	public void resetUI() {
		setTheme(mSettings.getTheme());
		setContentView(R.layout.browser);
		webview = (WebViewFixed) findViewById(R.id.webview);
		if (Util.isLightTheme(mSettings.getTheme())) {
			webview.setBackgroundResource(R.color.white);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
			webview.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.browser, menu);
		return  true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		if (mThreadUrl == null) {
			menu.findItem(R.id.view_comments_menu_id).setVisible(false);
		} else {
			menu.findItem(R.id.view_comments_menu_id).setVisible(true);
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.open_browser_menu_id:
				if (mUri == null) break;
				Common.launchBrowser(this, mUri.toString(), null, false, true, true, false);
				break;
			
			case R.id.close_browser_menu_id:
				finish();
				break;
				
			case R.id.view_comments_menu_id:
				if (mThreadUrl == null) break;
				Intent intent = new Intent(this, CommentsListActivity.class);
				intent.setData(Uri.parse(mThreadUrl));
				intent.putExtra(Constants.EXTRA_NUM_COMMENTS, Constants.DEFAULT_COMMENT_DOWNLOAD_LIMIT);
				startActivity(intent);
				break;
				
			case android.R.id.home:
				Common.goHome(this);
				break;
			
			default:
				throw new IllegalArgumentException("Unexpected action value " + item.getItemId());
		}
		return true;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		webview.saveState(outState);
	}
}
