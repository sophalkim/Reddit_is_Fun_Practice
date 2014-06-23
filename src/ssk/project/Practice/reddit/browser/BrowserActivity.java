package ssk.project.Practice.reddit.browser;

import java.lang.reflect.Method;

import ssk.project.Practice.settings.RedditSettings;
import android.app.Activity;
import android.net.Uri;
import android.util.Log;
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
}
