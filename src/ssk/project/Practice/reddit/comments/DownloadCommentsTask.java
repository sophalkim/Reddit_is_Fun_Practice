package ssk.project.Practice.reddit.comments;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;

import org.apache.http.client.HttpClient;
import org.codehaus.jackson.map.ObjectMapper;

import ssk.project.Practice.common.Common;
import ssk.project.Practice.settings.RedditSettings;
import android.os.AsyncTask;

import com.andrewshu.android.reddit.comments.CommentsListActivity;
import com.andrewshu.android.reddit.markdown.Markdown;
import com.andrewshu.android.reddit.things.ThingInfo;
import com.andrewshu.android.reddit.threads.ShowThumbnailsTask;

public class DownloadCommentsTask extends AsyncTask<Integer, Long, Boolean> implements PropertyChangeListener {

	private static final String TAG = "CommentsListActivity.DownloadCommentsTask";
	private final ObjectMapper mObjectMapper = Common.getObjectMapper();
	private final Markdown markdown = new Markdown();
	
	private static AsyncTask<?, ?, ?> mCurrentDownloadCommentsTask = null;
	private static final Object mCurrentDownloadCommentsTaskLock = new Object();
	
	private ShowThumbnailsTask mCurrentShowThumbnailsTask = null;
	private final Object mCurrentShowThumbnailsTaskLock = new Object();
	
	private ProcessCommentsTask mProcessCommentsTask;
	
	private CommentsListActivity mActivity;
	private String mSubreddit;
	private String mThreadId;
	private String mThreadTitle;
	private RedditSettings mSettings;
	private HttpClient mClient;
	
	private int mPositionOffset = 0;
	private int mIndentation = 0;
	private String mMoreChildrenId = "";
	private ThingInfo mOpThingInfo = null;
	
	private long mContentLength = 0;
	
	private String mJumpToCommentId = "";
	private int mJumpToCommentFoundIndex = -1;
	
	private int mJumpToCommentContext = 0;
	
	private final LinkedList<ThingInfo> mDeferredAppendList = new LinkedList<ThingInfo>();
	private final LinkedList<ThingInfo> mDeferredReplacementList = new LinkedList<ThingInfo>();
	
	public DownloadCommentsTask(CommentsListActivity activity, String subreddit, String threadId, RedditSettings settings, HttpClient client) {
		mActivity = activity;
		mSubreddit = subreddit;
		mThreadId = threadId;
		mSettings = settings;
		mClient = client;
		mProcessCommentsTask = new ProcessCommentsTask(activity);
	}
	
	
	
	@Override
	protected Boolean doInBackground(Integer... params) {
		return null;
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		
	}

	
}
