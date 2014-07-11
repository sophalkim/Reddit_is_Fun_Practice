package ssk.project.Practice.reddit.comments;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.codehaus.jackson.map.ObjectMapper;

import ssk.project.Practice.common.Common;
import android.os.AsyncTask;

import com.andrewshu.android.reddit.markdown.Markdown;

public class DownloadCommentsTask extends AsyncTask<Integer, Long, Boolean> implements PropertyChangeListener {

	private static final String TAG = "CommentsListActivity.DownloadCommentsTask";
	private final ObjectMapper mObjectMapper = Common.getObjectMapper();
	private final Markdown markdown = new Markdown();
	
	private static AsyncTask<?, ?, ?> mCurrentDownloadCommentsTask = null;
	
	
	
	
	@Override
	protected Boolean doInBackground(Integer... params) {
		return null;
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		
	}

	
}
