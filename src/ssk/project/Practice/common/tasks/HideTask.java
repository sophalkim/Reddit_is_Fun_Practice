package ssk.project.Practice.common.tasks;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicNameValuePair;

import ssk.project.Practice.common.Common;
import ssk.project.Practice.common.RedditIsFunHttpClientFactory;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.andrewshu.android.reddit.common.Constants;
import com.andrewshu.android.reddit.settings.RedditSettings;
import com.andrewshu.android.reddit.things.ThingInfo;

public class HideTask extends AsyncTask<Void, Void, Boolean> {

	private static final String TAG = "HideTask";
	
	protected ThingInfo mTargetThreadInfo;
	protected String mUserError = "Error hiding thread";
	protected String mUrl;
	private RedditSettings mSettings;
	private Context mContext;
	protected boolean mHide;
	
	private final HttpClient mClient = RedditIsFunHttpClientFactory.getGzipHttpClient();
	
	public HideTask(boolean hide, ThingInfo mVoteTargetThreadInfo, RedditSettings mSettings, Context mContext) {
		this.mTargetThreadInfo = mVoteTargetThreadInfo;
		this.mSettings = mSettings;
		this.mContext = mContext;
		this.mHide = hide;
		if (hide) {
			mUrl = Constants.REDDIT_BASE_URL + "/api/hide";
		} else {
			mUrl = Constants.REDDIT_BASE_URL + "/api/unhide";
		}
	}
	
	@Override
	public void onPreExecute() {
		if (!mSettings.isLoggedIn()) {
			Common.showErrorToast("You must be logged in to hide/unhide a thread.", Toast.LENGTH_LONG, mContext);
			cancel(true);
		}
		if (mHide) {
			Toast.makeText(mContext, "Hidden.", Toast.LENGTH_SHORT).show();
			mTargetThreadInfo.setHidden(true);
		} else {
			Toast.makeText(mContext, "Unhidden.", Toast.LENGTH_SHORT).show();
			mTargetThreadInfo.setHidden(false);
		}
	}
	
	@Override
	protected Boolean doInBackground(Void... v) {
		String status = "";
		HttpEntity entity = null;
		
		if (!mSettings.isLoggedIn()) {
			mUserError = "You must be logged in to hide/unhide.";
			return false;
		}
		
		if (mSettings.getModhash() == null) {
			String modhash = Common.doUpdateModhash(mClient);
			if (modhash == null) {
				Common.doLogout(mSettings, mClient, mContext);
				if (Constants.LOGGING) Log.e(TAG, "updating hide status failed because doUpdateModhash() failed");
				return false;
			}
			mSettings.setModhash(modhash);
		}
		
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("id", mTargetThreadInfo.getName()));
		nvps.add(new BasicNameValuePair("uh", mSettings.getModhash().toString()));
		
		
		return false;
	}

}
