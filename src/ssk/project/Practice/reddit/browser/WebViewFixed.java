package ssk.project.Practice.reddit.browser;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;

import com.andrewshu.android.reddit.common.Constants;

public class WebViewFixed extends WebView {

	private static final String TAG = "WebView";
	
	public WebViewFixed(Context context) {
		super(context);
	}
	
	public WebViewFixed(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public WebViewFixed(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		try {
			super.onWindowFocusChanged(hasWindowFocus);
		} catch (NullPointerException ex) {
			if (Constants.LOGGING) Log.e(TAG, "WebView.onWindowFocusChanged", ex);
		}
	}
}
