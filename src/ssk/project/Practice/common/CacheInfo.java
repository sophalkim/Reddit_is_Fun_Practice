package ssk.project.Practice.common;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

import com.andrewshu.android.reddit.common.Constants;

public class CacheInfo implements Serializable {

	static final long serialVersionUID = 39;
	static final String TAG = "CacheInfo";
	static final Object CACHE_LOCK = new Object();
	
	public long subredditTime = 0;
	public long threadTime = 0;
	public long subredditListTime = 0;
	
	public String subredditUrl = null;
	public String threadUrl = null;
	public ArrayList<String> subredditList = null;
	
	public static FileInputStream writeThenRead(Context context, InputStream in, String filename) throws IOException { 
		synchronized (CACHE_LOCK) {
			FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
			byte[] buf = new byte[1024];
			int len = 0;
			long total = 0;
			while ((len = in.read(buf)) > 0) {
				fos.write(buf, 0, len);
				total += len;
			}
			if (Constants.LOGGING) Log.d(TAG, total + " bytes written to cache file: " + filename);
			fos.close();
			in.close();
		}
		return context.openFileInput(filename);
	}
	
	public static boolean checkFreshSubredditCache(Context context) {
		long time = System.currentTimeMillis();
		long subredditTime = getCachedSubredditTime(context);
		return time - subredditTime <= Constants.DEFAULT_FRESH_DURATION;
	}
	
	public static boolean checkFreshThreadCache(Context context) {
		long time = System.currentTimeMillis();
		long threadTime = getCachedThreadTime(context);
		return time - threadTime <= Constants.DEFAULT_FRESH_DURATION;
	}
	
	public static boolean checkFreshSubredditListCache(Context context) {
		long time = System.currentTimeMillis();
		long subredditTime = getCachedSubredditListTime(context);
		return time - subredditTime <= Constants.DEFAULT_FRESH_DURATION;
	}
	
	static CacheInfo getCacheInfo(Context context) throws IOException, ClassNotFoundException {
		CacheInfo ci;
		synchronized (CACHE_LOCK) {
			FileInputStream fis = context.openFileInput(Constants.FILENAME_CACHE_INFO);
			ObjectInputStream ois = new ObjectInputStream(fis);
			ci = (CacheInfo) ois.readObject();
			ois.close();
			fis.close();
		}
		return ci;
	}
	
	public static String getCachedSubredditUrl(Context context) {
		try {
			return getCacheInfo(context).subredditUrl;
		} catch (Exception e) {
			if (Constants.LOGGING) Log.e(TAG, "error w/ getCacheInfo", e);
			return null;
		}
	}
	
	static long getCachedSubredditTime(Context context) {
		try {
			return getCacheInfo(context).subredditTime;
		} catch (Exception e) {
			if (Constants.LOGGING) Log.e(TAG, "error w/ getCacheInfo", e);
			return 0;
		}
	}
	
	public static String getCachedThreadUrl(Context context) {
    	try {
    		return getCacheInfo(context).threadUrl;
    	} catch (Exception e) {
    		if (Constants.LOGGING) Log.e(TAG, "error w/ getCacheInfo", e);
    		return null;
    	}
    }
    
    static long getCachedThreadTime(Context context) {
    	try {
    		return getCacheInfo(context).threadTime;
    	} catch (Exception e) {
    		if (Constants.LOGGING) Log.e(TAG, "error w/ getCacheInfo", e);
    		return 0;
    	}
    }
    
    public static ArrayList<String> getCachedSubredditList(Context context) {
    	try {
    		return getCacheInfo(context).subredditList;
    	} catch (Exception e) {
    		if (Constants.LOGGING) Log.e(TAG, "error w/ getCacheInfo", e);
    		return null;
    	}
    }
    
    static long getCachedSubredditListTime(Context context) {
    	try {
    		return getCacheInfo(context).subredditListTime;
    	} catch (Exception e) {
    		if (Constants.LOGGING) Log.e(TAG, "error w/ getCacheInfo", e);
    		return 0;
    	}
    }
    
    @SuppressWarnings("unused")
    public static void invalidateAllCaches(Context context) {
    	if (!Constants.USE_COMMENTS_CACHE && !Constants.USE_THREADS_CACHE && !Constants.USE_SUBREDDITS_CACHE) return;
    	try {
    		synchronized (CACHE_LOCK) {
    			FileOutputStream fos = context.openFileOutput(Constants.FILENAME_CACHE_INFO, Context.MODE_PRIVATE);
    			ObjectOutputStream oos = new ObjectOutputStream(fos);
    			oos.writeObject(new CacheInfo());
    			oos.close();
    			fos.close();
    			if (Constants.LOGGING) Log.e(TAG, "invalidateAllCaches: wrote blank CacheInfo");
    		}
    	} catch (IOException e) {
    		if (Constants.LOGGING) Log.e(TAG, "invalidateAllCaches: Error writing CacheInfo", e);
    	}
    }
    
    @SuppressWarnings("unused")
	public
    static void invalidateCachedSubreddit(Context context) {
    	if (!Constants.USE_THREADS_CACHE)
    		return;
    	
    	CacheInfo ci = null;
    	try {
    		ci = getCacheInfo(context);
    	} catch (Exception e) {
    		if (Constants.LOGGING) Log.e(TAG, "error w/ getCacheInfo", e);
    	}
		if (ci == null)
			ci = new CacheInfo();
    	
    	try {
    		synchronized (CACHE_LOCK) {
		    	FileOutputStream fos = context.openFileOutput(Constants.FILENAME_CACHE_INFO, Context.MODE_PRIVATE);
		    	ObjectOutputStream oos = new ObjectOutputStream(fos);
		    	ci.subredditUrl = null;
		    	ci.subredditTime = 0;
		    	oos.writeObject(ci);
		    	oos.close();
		    	fos.close();
    		}
    	} catch (IOException e) {
    		if (Constants.LOGGING) Log.e(TAG, "invalidateCachedSubreddit: Error writing CacheInfo", e);
    	}
    }
    
    @SuppressWarnings("unused")
	public
    static void invalidateCachedThread(Context context) {
    	if (!Constants.USE_COMMENTS_CACHE)
    		return;
    	
    	CacheInfo ci = null;
    	try {
    		ci = getCacheInfo(context);
    	} catch (Exception e) {
    		if (Constants.LOGGING) Log.e(TAG, "error w/ getCacheInfo", e);
    	}
		if (ci == null)
			ci = new CacheInfo();
    	
    	try {
    		synchronized (CACHE_LOCK) {
		    	FileOutputStream fos = context.openFileOutput(Constants.FILENAME_CACHE_INFO, Context.MODE_PRIVATE);
		    	ObjectOutputStream oos = new ObjectOutputStream(fos);
		    	ci.threadUrl = null;
		    	ci.threadTime = 0;
		    	oos.writeObject(ci);
		    	oos.close();
		    	fos.close();
    		}
    	} catch (IOException e) {
    		if (Constants.LOGGING) Log.e(TAG, "invalidateCachedThreadId: Error writing CacheInfo", e);
    	}
    }
    
    @SuppressWarnings("unused")
	public
    static void setCachedSubredditUrl(Context context, String subredditUrl) throws IOException {
    	if (!Constants.USE_THREADS_CACHE)
    		return;
    	
    	CacheInfo ci = null;
    	try {
    		ci = getCacheInfo(context);
    	} catch (Exception e) {
    		if (Constants.LOGGING) Log.e(TAG, "error w/ getCacheInfo", e);
    	}
		if (ci == null)
			ci = new CacheInfo();
    	
		synchronized (CACHE_LOCK) {
			FileOutputStream fos = context.openFileOutput(Constants.FILENAME_CACHE_INFO, Context.MODE_PRIVATE);
	    	ObjectOutputStream oos = new ObjectOutputStream(fos);
	    	ci.subredditUrl = subredditUrl;
	    	ci.subredditTime = System.currentTimeMillis();
	    	oos.writeObject(ci);
	    	oos.close();
	    	fos.close();
		}
    }

    @SuppressWarnings("unused")
	public
    static void setCachedThreadUrl(Context context, String threadUrl) throws IOException {
    	if (!Constants.USE_COMMENTS_CACHE)
    		return;
    	
    	CacheInfo ci = null;
    	try {
    		ci = getCacheInfo(context);
    	} catch (Exception e) {
    		if (Constants.LOGGING) Log.e(TAG, "error w/ getCacheInfo", e);
    	}
		if (ci == null)
			ci = new CacheInfo();

		synchronized (CACHE_LOCK) {
			FileOutputStream fos = context.openFileOutput(Constants.FILENAME_CACHE_INFO, Context.MODE_PRIVATE);
	    	ObjectOutputStream oos = new ObjectOutputStream(fos);
	    	ci.threadUrl = threadUrl;
	    	ci.threadTime = System.currentTimeMillis();
	    	oos.writeObject(ci);
	    	oos.close();
	    	fos.close();
		}
    }
    
    public static void setCachedSubredditList(Context context, ArrayList<String> subredditList) throws IOException {
    	if (!Constants.USE_SUBREDDITS_CACHE) return;
    	CacheInfo ci = null;
    	try {
    		ci = getCacheInfo(context);
    	} catch (Exception e) {
    		if (Constants.LOGGING) Log.e(TAG, "error w/ getCacheInfo", e);
    	}
    	if (ci == null) ci = new CacheInfo();
    	synchronized (CACHE_LOCK) {
    		FileOutputStream fos = context.openFileOutput(Constants.FILENAME_CACHE_INFO, Context.MODE_PRIVATE);
    		ObjectOutputStream oos = new ObjectOutputStream(fos);
    		ci.subredditList = subredditList;
    		ci.subredditListTime = System.currentTimeMillis();
    		oos.writeObject(ci);
    		oos.close();
    		fos.close();
    	}
    }
}
