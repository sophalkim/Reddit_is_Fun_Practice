package ssk.project.Practice.util;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;

import android.app.Activity;
import android.net.Uri;
import android.text.style.URLSpan;
import android.util.Log;

import com.andrewshu.android.reddit.R;
import com.andrewshu.android.reddit.common.Common;
import com.andrewshu.android.reddit.common.Constants;
import com.andrewshu.android.reddit.things.ThingInfo;

public class Util {

	private static final String TAG = "Util";
	
	public static ArrayList<String> extractUris(URLSpan[] spans) {
		int size = spans.length;
		ArrayList<String> accumulator = new ArrayList<String>();
		for (int i = 0; i < size; i++) {
			accumulator.add(spans[i].getURL());
		}
		return accumulator;
	}
	
	public static String convertHtmlTags(String html) {
		html = html.replaceAll("<code>", "<tt>").replaceAll("</code>", "</tt>");
		int preIndex = html.indexOf("<pre>");
		int preEndIndex = -6;
		StringBuilder bodyConverted = new StringBuilder();
		while (preIndex != -1) {
			bodyConverted = bodyConverted.append(html.substring(preEndIndex + 6, preIndex));
			preEndIndex = html.indexOf("</pre>", preIndex);
			bodyConverted = bodyConverted.append(html.substring(preIndex, preEndIndex).replaceAll("\n", "<br>")).append("</pre>");
			preIndex = html.indexOf("<pre>", preEndIndex);
		}
		html = bodyConverted.append(html.substring(preEndIndex + 6)).toString();
		html = html.replaceAll("<li>(<p>)?", "&#8226; ").replaceAll("(</p>)?</li>", "<br>");
		html = html.replaceAll("<strong>", "<b>").replaceAll("</strong>", "</b>")
				.replaceAll("<em>", "<i>").replaceAll("</em>", "</i>");
		return html;
	}
	
	public static String getTimeAgo(long utcTimeSeconds) {
		long systime = System.currentTimeMillis() / 1000;
		long diff = systime - utcTimeSeconds;
		if (diff <= 0) return "very recently";
		else if (diff < 60) {
			if (diff == 1)
				return "1 second ago";
			else 
				return diff + " seconds ago";
		}
		else if (diff < 3600) {
			if ((diff / 60) == 1)
				return "1 minute ago";
			else 
				return (diff / 60) + " minutes ago";
		}
		else if (diff < 86400) {
			if ((diff / 3600 == 1))
				return "1 hour ago";
			else 
				return (diff / 3600) + " hours ago";
		}
		else if (diff < 604800) { // 86400 * 7
			if ((diff / 86400) == 1)
				return "1 day ago";
			else
				return (diff / 86400) + " days ago";
		}
		else if (diff < 2592000) { // 86400 * 30
			if ((diff / 604800) == 1)
				return "1 week ago";
			else
				return (diff / 604800) + " weeks ago";
		}
		else if (diff < 31536000) { // 86400 * 365
			if ((diff / 2592000) == 1)
				return "1 month ago";
			else
				return (diff / 2592000) + " months ago";
		}
		else {
			if ((diff / 31536000) == 1)
				return "1 year ago";
			else
				return (diff / 31536000) + " years ago";
		}
	}
	
	public static String getTimeAgo(double utcTimeSeconds) {
		return getTimeAgo((long)utcTimeSeconds);
	}
	
	public static String showNumComments(int comments) {
		if (comments == 1) {
			return "1 comment";
		} else {
			return comments + " comments";
		}
	}
	
	public static String showNumPoints(int score) {
		if (score == 1) {
			return "1 point";
		} else {
			return score + " points";
		}
	}
	
	public static String absolutePathToURL(String path) {
		if (path.startsWith("/"))
			return Constants.REDDIT_BASE_URL + path;
		return path;
	}
	
	public static String nameToId(String name) {
		return name.substring(name.indexOf('_') + 1);
	}
	
	public static boolean isHttpStatusOK(HttpResponse response) {
		if (response == null || response.getStatusLine() == null) {
			return false;
		}
		return response.getStatusLine().getStatusCode() == 200;
	}
	
	public static String getResponseErrorMessage(String line) throws Exception {
		String error = null;
		if (StringUtils.isEmpty(line)) {
			error = "Connection error when subscribing. Try again.";
			throw new HttpException("No content returned from subscribe POST");
		}
		if (line.contains("WRONG_PASSWORD")) {
			error = "Wrong Password.";
			throw new Exception("Wrong password.");
		}
		if (line.contains("USER_REQUIRED")) {
    		// The modhash probably expired
    		throw new Exception("User required. Huh?");
    	}
		Common.logDLong(TAG, line);
		return error;
	}
	
	public static boolean isLightTheme(int theme) {
		return theme == R.style.Reddit_Light_Medium || theme == R.style.Reddit_Light_Large || theme == R.style.Reddit_Light_Larger || theme == R.style.Reddit_Light_Huge;
	}
	
	public static int getInvertedTheme(int theme) {
		switch (theme) {
		case R.style.Reddit_Light_Medium:
			return R.style.Reddit_Dark_Medium;
		case R.style.Reddit_Light_Large:
			return R.style.Reddit_Dark_Large;
		case R.style.Reddit_Light_Larger:
			return R.style.Reddit_Dark_Larger;
		case R.style.Reddit_Light_Huge:
			return R.style.Reddit_Dark_Huge;
		case R.style.Reddit_Dark_Medium:
			return R.style.Reddit_Light_Medium;
		case R.style.Reddit_Dark_Large:
			return R.style.Reddit_Light_Large;
		case R.style.Reddit_Dark_Larger:
			return R.style.Reddit_Light_Larger;
		case R.style.Reddit_Dark_Huge:
			return R.style.Reddit_Light_Huge;
		default:
			return R.style.Reddit_Light_Medium;
		}
	}
	
	public static int getThemeResourceFromPrefs(String themePref, String textSizePref) {
		if (Constants.PREF_THEME_LIGHT.equals(themePref)) {
			if (Constants.PREF_TEXT_SIZE_MEDIUM.equals(textSizePref))
				return R.style.Reddit_Light_Medium;
			else if (Constants.PREF_TEXT_SIZE_LARGE.equals(textSizePref))
				return R.style.Reddit_Light_Large;
			else if (Constants.PREF_TEXT_SIZE_LARGER.equals(textSizePref))
				return R.style.Reddit_Light_Larger;
			else if (Constants.PREF_TEXT_SIZE_HUGE.equals(textSizePref))
				return R.style.Reddit_Light_Huge;
		} else /* if (Constants.PREF_THEME_DARK.equals(themePref)) */ {
			if (Constants.PREF_TEXT_SIZE_MEDIUM.equals(textSizePref))
				return R.style.Reddit_Dark_Medium;
			else if (Constants.PREF_TEXT_SIZE_LARGE.equals(textSizePref))
				return R.style.Reddit_Dark_Large;
			else if (Constants.PREF_TEXT_SIZE_LARGER.equals(textSizePref))
				return R.style.Reddit_Dark_Larger;
			else if (Constants.PREF_TEXT_SIZE_HUGE.equals(textSizePref))
				return R.style.Reddit_Dark_Huge;
		}
		return R.style.Reddit_Light_Medium;
	}
	
	public static long getMillisFromMailNotificationPref(String pref) {
		if (Constants.PREF_MAIL_NOTIFICATION_SERVICE_OFF.equals(pref)) {
			return 0;
		} else if (Constants.PREF_MAIL_NOTIFICATION_SERVICE_5MIN.equals(pref)) {
			return 5 * 60 * 1000;
		} else if (Constants.PREF_MAIL_NOTIFICATION_SERVICE_30MIN.equals(pref)) {
			return 30 * 60 * 1000;
		} else if (Constants.PREF_MAIL_NOTIFICATION_SERVICE_1HOUR.equals(pref)) {
			return 1 * 3600 * 1000;
		} else if (Constants.PREF_MAIL_NOTIFICATION_SERVICE_6HOURS.equals(pref)) {
			return 6 * 3600 * 1000;
		} else /* if (Constants.PREF_MAIL_NOTIFICATION_SERVICE_1DAY.equals(pref)) */ {
			return 24 * 3600 * 1000;
		}
	}
	
	public static void overridePendingTransition(Method activity_overridePendingTransition, Activity act, int enterAnim, int exitAnim) {
		if (activity_overridePendingTransition != null) {
			try {
				activity_overridePendingTransition.invoke(act, enterAnim, exitAnim);
			} catch (Exception ex) {
				if (Constants.LOGGING) Log.e(TAG, "overridePendingTransition", ex);
			}
		}
	}
	
	static Uri createCommentUri(String linkId, String commentId, int commentContext) {
		return Uri.parse(new StringBuilder(Constants.REDDIT_BASE_URL + "/comments")
				.append(linkId)
				.append("/z/")
				.append(commentId)
				.append("?context=")
				.append(commentContext)
				.toString());
	}
	
	public static Uri createCommentUri(ThingInfo commentThingInfo, int commentContext) {
		if (commentThingInfo.getContext() != null)
			return Uri.parse(absolutePathToURL(commentThingInfo.getContext()));
		if (commentThingInfo.getLink_id() != null)
			return createCommentUri(nameToId(commentThingInfo.getLink_id()), commentThingInfo.getId(), commentContext);
		return null;
	}
	
	public static Uri createProfileUri(String username) {
		return Uri.parse(new StringBuilder(Constants.REDDIT_BASE_URL + "/user/")
				.append(username)
				.toString());
	}
	
	public static Uri createSubmitUri(String subreddit) {
		if (Constants.FRONTPAGE_STRING.equals(subreddit))
			return Uri.parse(Constants.REDDIT_BASE_URL + "/submit/");
		return Uri.parse(new StringBuilder(Constants.REDDIT_BASE_URL + "/r/")
				.append(subreddit)
				.append("/submit")
				.toString());
	}
	
	static Uri createSubmitUri(ThingInfo thingInfo) {
		return createSubmitUri(thingInfo.getSubreddit());
	}
	
	public static Uri createSubredditUri(String subreddit) {
		if (Constants.FRONTPAGE_STRING.equals(subreddit))
			return Uri.parse(Constants.REDDIT_BASE_URL + "/");
		
		return Uri.parse(new StringBuilder(Constants.REDDIT_BASE_URL + "/r")
		.append(subreddit)
		.toString());
	}
	
	static Uri createSubredditUri(ThingInfo thingInfo) {
		return createSubredditUri(thingInfo.getSubreddit());
	}
	
	static Uri createThreadUri(String subreddit, String threadId) {
		return Uri.parse(new StringBuilder(Constants.REDDIT_BASE_URL + "/r/")
				.append(subreddit)
				.append("/comments/")
				.append(threadId)
				.toString()); 
	}
	
	public static Uri createThreadUri(ThingInfo threadThingInfo) {
		return createThreadUri(threadThingInfo.getSubreddit(), threadThingInfo.getId());
	}
	
	public static boolean isRedditUri(Uri uri) {
		if (uri == null) return false;
		String host = uri.getHost();
		return host != null && (host.equals("reddit.com") || host.endsWith(".reddit.com"));
	}
	
	public static boolean isRedditShortenedUri(Uri uri) {
		if (uri == null) return false;
		String host = uri.getHost();
		return host != null && host.equals("redd.it");
	}
	
	public static Uri optimizeMobileUri(Uri uri) {
		if (isNonMobileWikipediaUri(uri)) {
			uri = createMobileWikpediaUri(uri);
		}
		return uri;
	}
	
	static boolean isNonMobileWikipediaUri(Uri uri) {
		if (uri == null) return false;
		String host = uri.getHost();
		return host != null && host.endsWith(".wikipedia.org") && !host.contains(".m.wikipedia.org");
	}
	
	static Uri createMobileWikpediaUri(Uri uri) {
		String uriString = uri.toString();
		return Uri.parse(uriString.replace(".wikipedia.org/", ".m.wikipedia.org"));
	}
	
	public static boolean isYoutubeUri(Uri uri) {
		if (uri == null) return false;
		String host = uri.getHost();
		return host != null && (host.endsWith(".youtube.com") || host.equals("youtu.be"));
	}
	
	public static boolean isAndroidMarketUri(Uri uri) {
		if (uri == null) return false;
		String host = uri.getHost();
		return host != null && host.equals("market.android.com");
	}
}
