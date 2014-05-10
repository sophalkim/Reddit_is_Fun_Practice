package ssk.project.Practice.common.tasks;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import ssk.project.Practice.common.Common;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
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
		
		if (mSettings.getModhash() == null) {
			String modhash = Common.doUpdateModhash(mClient);
			if (modhash == null) {
				Common.doLogout(mSettings, mClient, mContext);
				if (Constants.LOGGING) Log.e(TAG, "updating save status failed because doUpdateModhash() failed");
				return false;
			}
			mSettings.setModhash(modhash);
		}
		
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("id", mTargetThreadInfo.getName()));
		nvps.add(new BasicNameValuePair("uh", mSettings.getModhash().toString()));
		
		try {
			HttpPost request = new HttpPost(mUrl);
			request.setHeader("Content-Type", "application/x-www-form-urlencoded");
			request.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		}
		return false;
	}

	
}
