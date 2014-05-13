package ssk.project.Practice.common;

public class Constants {

	public static final boolean LOGGING = true;
	
	public static final boolean USE_COMMENT_CACHE = false;
	public static final boolean USE_THREADS_CACHE = false;
	public static final boolean USE_SUBREDDITS_CACHE = true;
	
	public static final String FILENAME_SUBREDDIT_CACHE = "subreddit.dat";
	public static final String FILENAME_THREAD_CACHE = "thread.dat";
	public static final String FILENAME_CACHE_INFO = "cacheinfo.dat";
	public static final String[] FILENAME_CACHE = {
		FILENAME_SUBREDDIT_CACHE, FILENAME_THREAD_CACHE, FILENAME_CACHE_INFO
	};
	
	public static final long MESSAGE_CHECK_MINIMUM_INTERVAL_MILLIS = 5 * 60 * 1000;
	public static final String LAST_MAIL_CHECK_TIME_MILLIS_KEY = "LAST_MAIL_CHECK_TIME_MILLIS_KEY";
	
	public static final String COMMENT_PATH_PATTERN_STRING = "(?:/r/([^/]+)/comments|/comments|/tb)/([^/]+)(?:/?$|/[^/]+/([a-zA-Z0-9]+)?)?";
	public static final String REDDIT_PATH_PATTERN_STRING = "(?:/r/([^/]+))?/?$";
	public static final String USER_PATH_PATTERN_STRING = "/user/([^/]+)/?$";
	
	public static final String COMMENT_KIND = "t1";
	public static final String THREAD_KIND = "t3";
	public static final String MESSAGE_KIND = "t4";
	public static final String SUBREDDIT_KIND = "t5";
	public static final String MORE_KIND = "more";
	
	public static final int DEFAULT_THREAD_DOWNLOAD_LIMIT = 25;
	public static final int DEFAULT_COMMENT_DOWNLOAD_LIMIT = 200;
	public static final long DEFAULT_FRESH_DURATION = 18000000;
	public static final long DEFAULT_FRESH_SUBREDDIT_LIST_DURATION = 86400000;
	
}
