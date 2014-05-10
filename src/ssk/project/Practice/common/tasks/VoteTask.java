package ssk.project.Practice.common.tasks;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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

import com.andrewshu.android.reddit.common.Constants;
import com.andrewshu.android.reddit.settings.RedditSettings;

public class VoteTask extends AsyncTask<Void, Void, Boolean> {

	private static final String TAG = "VoteTask";
	
	protected String _mThingFullname, _mSubreddit;
	protected int _mDirection;
	protected String _mUserError = "Error voting.";
	
	protected RedditSettings _mSettings;
	protected HttpClient _mClient;
	protected Context _mContext;
	
	public VoteTask(String thingFullname, int direction, String subreddit,
			Context context, RedditSettings settings, HttpClient client) {
		_mClient = client;
		_mSettings = settings;
		_mContext = context;
		_mThingFullname = thingFullname;
		_mDirection = direction;
		_mSubreddit = subreddit;
	}
	
	@Override
	protected Boolean doInBackground(Void... v) {
		HttpEntity entity = null;
		
		if (!_mSettings.isLoggedIn()) {
			_mUserError = "You must be logged in to vote.";
		}
		
		if (_mSettings.getModhash() == null) {
			String modhash = Common.doUpdateModhash(_mClient);
			if (modhash == null) {
				Common.doLogout(_mSettings, _mClient, _mContext);
				if (Constants.LOGGING) Log.e(TAG, "Vote failed because doUpdateModhash() failed");
				return false;
			}
			_mSettings.setModhash(modhash);
		}
		
		try {
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("id", _mThingFullname));
			nvps.add(new BasicNameValuePair("dir", String.valueOf(_mDirection)));
			nvps.add(new BasicNameValuePair("r", _mSubreddit));
			nvps.add(new BasicNameValuePair("uh", _mSettings.getModhash()));
			
			HttpPost httppost = new HttpPost(Constants.REDDIT_BASE_URL + "/api/vote");
			httppost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			
			if (Constants.LOGGING) Log.d(TAG, nvps.toString());
			
			HttpResponse response = _mClient.execute(httppost);
			entity = response.getEntity();
			
			String error = Common.checkResponseError(response, entity);
			if (error != null) {
				throw new Exception(error);
			}
			return true;
		} catch (Exception e) {
			if (Constants.LOGGING) Log.e(TAG, "VoteTask", e);
			_mUserError = e.getMessage();
		} finally {
			if (entity != null) {
				try {
					entity.consumeContent();
				} catch (Exception e2) {
					if (Constants.LOGGING) Log.e(TAG, "entity.consumeContent()", e2);
				}
			}
		}
		return false;
	}

	
}
