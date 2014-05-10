package ssk.project.Practice.common.tasks;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;

import ssk.project.Practice.common.Common;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.andrewshu.android.reddit.common.Constants;
import com.andrewshu.android.reddit.common.RedditIsFunHttpClientFactory;
import com.andrewshu.android.reddit.settings.RedditSettings;
import com.andrewshu.android.reddit.things.ThingInfo;

public class SaveTask extends AsyncTask<Void, Void, Boolean> {

	private static final String TAG = "SaveWorker";
	
	private ThingInfo mTargetThreadInfo;
	private String mUserError = "Error voting.";
	private String mUrl;
	private boolean mSave;
	private RedditSettings mSettings;
	private Context mContext;
	
	private final HttpClient mClient = RedditIsFunHttpClientFactory.getGzipHttpClient();
	
	public SaveTask(boolean mSave, ThingInfo mVoteTargetThreadInfo, RedditSettings mSettings, Context mContext) {
		if (mSave) {
			mUrl = Constants.REDDIT_BASE_URL + "/api/save";
		} else {
			mUrl = Constants.REDDIT_BASE_URL + "/api/unsave";
		}
		this.mSave = mSave;
		mTargetThreadInfo = mVoteTargetThreadInfo;
		this.mSettings = mSettings;
		this.mContext = mContext;
	}
	
	@Override
	public void onPreExecute() {
		if (!mSettings.isLoggedIn()) {
			Common.showErrorToast("You must be logged in to save.", Toast.LENGTH_LONG, mContext);
			cancel(true);
			return;
		}
		if (mSave) {
			mTargetThreadInfo.setSaved(true);
			Toast.makeText(mContext, "Saved!", Toast.LENGTH_SHORT).show();
		} else {
			mTargetThreadInfo.setSaved(false);
			Toast.makeText(mContext, "Unsaved!", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	protected Boolean doInBackground(Void... v) {
		String status = "";
		HttpEntity entity = null;
		
		if (!mSettings.isLoggedIn()) {
			mUserError = "You must be logged in to save";
			return false;
		}
		
		
		return false;
	}

	
}
