package ssk.project.Practice.settings;

import java.util.Date;

import org.apache.http.client.HttpClient;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;

import ssk.project.Practice.util.StringUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.CookieSyncManager;

import com.andrewshu.android.reddit.R;
import com.andrewshu.android.reddit.common.Constants;
import com.andrewshu.android.reddit.common.RedditIsFunHttpClientFactory;
import com.andrewshu.android.reddit.common.util.Util;

public class RedditSettings {

	private static final String TAG = "RedditSettings";
	
	private String username = null;
	private Cookie redditSessionCookie = null;
	private String modhash = null;
	private String homepage = Constants.FRONTPAGE_STRING;
	private boolean useExternalBrowser = false;
	private boolean showCommentGuideLines = true;
	private boolean confirmQuitOrLogout = true;
	private boolean saveHistory = true;
	private boolean alwaysShowNextPrevious = true;
	
	private int threadDownloadLimit = Constants.DEFAULT_THREAD_DOWNLOAD_LIMIT;
	private String commentsSortByUrl = Constants.CommentsSort.SORT_BY_BEST_URL;
	
	private int theme = R.style.Reddit_Light_Medium;
	private int rotation = -1;
	private boolean loadThumbnails = true;
	private boolean loadThumbnailsOnlyWifi = false;
	
	private String mailNotificationStyle = Constants.PREF_MAIL_NOTIFICATION_STYLE_DEFAULT;
	private String mailNotificationService = Constants.PREF_MAIL_NOTIFICATION_SERVICE_OFF;
	
	public static class Rotation {
		
		public static int valueOf(String valueString) {
			if (Constants.PREF_ROTATION_UNSPECIFIED.equals(valueString)) {
				return -1;
			}
			if (Constants.PREF_ROTATION_PORTRAIT.equals(valueString)) {
				return 1;
			}
			if (Constants.PREF_ROTATION_LANDSCAPE.equals(valueString)) {
				return 0;
			}
			return -1;
		}
		
		public static String toString(int value) {
			switch (value) {
			case -1:
				return Constants.PREF_ROTATION_UNSPECIFIED;
			case 1:
				return Constants.PREF_ROTATION_PORTRAIT;
			case 0:
				return Constants.PREF_ROTATION_LANDSCAPE;
			default:
				return Constants.PREF_ROTATION_UNSPECIFIED;
			}
		}
	}
	
	public void saveRedditPreferences(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		
		if (username != null) {
			editor.putString("username", username);
		} else {
			editor.remove("username");
		}
		if (redditSessionCookie != null) {
			editor.putString("reddit_sessionValue", redditSessionCookie.getValue());
			editor.putString("reddit_sessionDomain", redditSessionCookie.getDomain());
			editor.putString("reddit_sessionPath", redditSessionCookie.getPath());
			if (redditSessionCookie.getExpiryDate() != null) {
				editor.putLong("reddit_sessionExpiryDate", redditSessionCookie.getExpiryDate().getTime());
			}
		}
		if (modhash != null) {
			editor.putString("modhash", modhash.toString());
		}
		editor.putString(Constants.PREF_HOMEPAGE, homepage.toString());
		editor.putBoolean(Constants.PREF_USE_EXTERNAL_BROWSER, useExternalBrowser);
		editor.putBoolean(Constants.PREF_CONFIRM_QUIT, confirmQuitOrLogout);
		editor.putBoolean(Constants.PREF_SAVE_HISTORY, saveHistory);
		editor.putBoolean(Constants.PREF_ALWAYS_SHOW_NEXT_PREVIOUS, alwaysShowNextPrevious);
		editor.putString(Constants.PREF_COMMENTS_SORT_BY_URL, commentsSortByUrl);
		
		String[] themeTextSize = Util.getPrefsFromThemeResource(theme);
		editor.putString(Constants.PREF_THEME, themeTextSize[0]);
		editor.putString(Constants.PREF_TEXT_SIZE, themeTextSize[1]);
		
		editor.putBoolean(Constants.PREF_SHOW_COMMENT_GUIDE_LINES, showCommentGuideLines);
		editor.putString(Constants.PREF_ROTATION, RedditSettings.Rotation.toString(rotation));
		editor.putBoolean(Constants.PREF_LOAD_THUMBNAILS, loadThumbnails);
		editor.putBoolean(Constants.PREF_LOAD_THUMBNAILS_ONLY_WIFI, loadThumbnailsOnlyWifi);
		
		editor.putString(Constants.PREF_MAIL_NOTIFICATION_STYLE, mailNotificationStyle);
		editor.putString(Constants.PREF_MAIL_NOTIFICATION_SERVICE, mailNotificationService);
		
		editor.commit();
	}
	
	public void loadRedditPreferences(Context context, HttpClient client) {
		SharedPreferences sessionPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		setUsername(sessionPrefs.getString("username", null));
		setModhash(sessionPrefs.getString("modhash", null));
		String cookieValue = sessionPrefs.getString("reddit_sessionValue", null);
		String cookieDomain = sessionPrefs.getString("reddit_sessionDomain", null);
		String cookiePath = sessionPrefs.getString("reddit_sessionPath", null);
		long cookieExpiryDate = sessionPrefs.getLong("reddit_sessionExpiryDate", -1);
		if (cookieValue != null) {
			BasicClientCookie redditSessionCookie = new BasicClientCookie("reddit_session", cookieValue);
			redditSessionCookie.setDomain(cookieDomain);
			redditSessionCookie.setPath(cookiePath);
			if (cookieExpiryDate != -1) {
				redditSessionCookie.setExpiryDate(new Date(cookieExpiryDate));
			} else {
				redditSessionCookie.setExpiryDate(null);
			}
			setRedditSessionCookie(redditSessionCookie);
			RedditIsFunHttpClientFactory.getCookieStore().addCookie(redditSessionCookie);
			try {
				CookieSyncManager.getInstance().sync();
			} catch (IllegalStateException ex) {
				if (Constants.LOGGING) Log.e(TAG, "CookieSyncManager.getInstsance().sync()", ex);
			}
		}
		
		String homepage = sessionPrefs.getString(Constants.PREF_HOMEPAGE, Constants.FRONTPAGE_STRING).trim();
		if (StringUtils.isEmpty(homepage)) {
			setHomepage(Constants.FRONTPAGE_STRING);
		} else {
			setHomepage(homepage);
		}
		setUseExternalBrowser(sessionPrefs.getBoolean(Constants.PREF_USE_EXTERNAL_BROWSER, false));
		setConfirmQuitOrLogout(sessionPrefs.getBoolean(Constants.PREF_CONFIRM_QUIT, true));
		setSaveHistory(sessionPrefs.getBoolean(Constants.PREF_SAVE_HISTORY, true));
		setAlwaysShowNextPrevious(sessionPrefs.getBoolean(Constants.PREF_ALWAYS_SHOW_NEXT_PREVIOUS, true));
		setCommentsSortByUrl(sessionPrefs.getString(Constants.PREF_COMMENTS_SORT_BY_URL, Constants.CommentsSort.SORT_BY_BEST_URL));
		
		
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Cookie getRedditSessionCookie() {
		return redditSessionCookie;
	}

	public void setRedditSessionCookie(Cookie redditSessionCookie) {
		this.redditSessionCookie = redditSessionCookie;
	}

	public String getModhash() {
		return modhash;
	}

	public void setModhash(String modhash) {
		this.modhash = modhash;
	}

	public String getHomepage() {
		return homepage;
	}

	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}

	public boolean isUseExternalBrowser() {
		return useExternalBrowser;
	}

	public void setUseExternalBrowser(boolean useExternalBrowser) {
		this.useExternalBrowser = useExternalBrowser;
	}

	public boolean isShowCommentGuideLines() {
		return showCommentGuideLines;
	}

	public void setShowCommentGuideLines(boolean showCommentGuideLines) {
		this.showCommentGuideLines = showCommentGuideLines;
	}

	public boolean isConfirmQuitOrLogout() {
		return confirmQuitOrLogout;
	}

	public void setConfirmQuitOrLogout(boolean confirmQuitOrLogout) {
		this.confirmQuitOrLogout = confirmQuitOrLogout;
	}

	public boolean isSaveHistory() {
		return saveHistory;
	}

	public void setSaveHistory(boolean saveHistory) {
		this.saveHistory = saveHistory;
	}

	public boolean isAlwaysShowNextPrevious() {
		return alwaysShowNextPrevious;
	}

	public void setAlwaysShowNextPrevious(boolean alwaysShowNextPrevious) {
		this.alwaysShowNextPrevious = alwaysShowNextPrevious;
	}

	public int getThreadDownloadLimit() {
		return threadDownloadLimit;
	}

	public void setThreadDownloadLimit(int threadDownloadLimit) {
		this.threadDownloadLimit = threadDownloadLimit;
	}

	public String getCommentsSortByUrl() {
		return commentsSortByUrl;
	}

	public void setCommentsSortByUrl(String commentsSortByUrl) {
		this.commentsSortByUrl = commentsSortByUrl;
	}

	public int getTheme() {
		return theme;
	}

	public void setTheme(int theme) {
		this.theme = theme;
	}

	public int getRotation() {
		return rotation;
	}

	public void setRotation(int rotation) {
		this.rotation = rotation;
	}

	public boolean isLoadThumbnails() {
		return loadThumbnails;
	}

	public void setLoadThumbnails(boolean loadThumbnails) {
		this.loadThumbnails = loadThumbnails;
	}

	public boolean isLoadThumbnailsOnlyWifi() {
		return loadThumbnailsOnlyWifi;
	}

	public void setLoadThumbnailsOnlyWifi(boolean loadThumbnailsOnlyWifi) {
		this.loadThumbnailsOnlyWifi = loadThumbnailsOnlyWifi;
	}

	public String getMailNotificationStyle() {
		return mailNotificationStyle;
	}

	public void setMailNotificationStyle(String mailNotificationStyle) {
		this.mailNotificationStyle = mailNotificationStyle;
	}

	public String getMailNotificationService() {
		return mailNotificationService;
	}

	public void setMailNotificationService(String mailNotificationService) {
		this.mailNotificationService = mailNotificationService;
	}

	public static String getTag() {
		return TAG;
	}
	
	
	
}
