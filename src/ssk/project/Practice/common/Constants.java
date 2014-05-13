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
	
}
