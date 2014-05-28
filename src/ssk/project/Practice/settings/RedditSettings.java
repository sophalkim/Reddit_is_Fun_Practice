package ssk.project.Practice.settings;

import org.apache.http.cookie.Cookie;

import com.andrewshu.android.reddit.R;

import com.andrewshu.android.reddit.common.Constants;

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
	
	
	
}
