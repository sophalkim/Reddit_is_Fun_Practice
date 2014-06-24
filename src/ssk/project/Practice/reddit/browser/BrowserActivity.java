package ssk.project.Practice.reddit.browser;

import java.lang.reflect.Method;

import ssk.project.Practice.settings.RedditSettings;
import android.app.Activity;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

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
}
