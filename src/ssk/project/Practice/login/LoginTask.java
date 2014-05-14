package ssk.project.Practice.login;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.codehaus.jackson.JsonFactory;

import ssk.project.Practice.common.Common;
import ssk.project.Practice.util.StringUtils;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.andrewshu.android.reddit.common.Constants;
import com.andrewshu.android.reddit.common.RedditIsFunHttpClientFactory;
import com.andrewshu.android.reddit.settings.RedditSettings;

public class LoginTask extends AsyncTask<Void, Void, Boolean> {

	private static final String TAG = "LoginTask";
	
	protected String mUsername;
	private String mPassword;
	protected String mUserError = null;
	
	private RedditSettings mSettings;
	private HttpClient mClient;
	private Context mContext;
	
	protected LoginTask(String username, String password, RedditSettings settings, Context context, HttpClient client) {
		mUsername = username;
		mPassword = password;
		mSettings = settings;
		mContext = context;
		mClient = client;
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
		return doLogin(mUsername, mPassword, mSettings, mClient, mContext);
	}
	
	private Boolean doLogin(String username, String password, RedditSettings settings, HttpClient client, Context context) {
		String status = "";
		String userError = "Error logging in. Please try again";
		HttpEntity entity = null;
		
		try {
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("user", username.toString()));
			nvps.add(new BasicNameValuePair("password", password.toString()));
			nvps.add(new BasicNameValuePair("api_type", "json"));
			
			HttpPost httppost = new HttpPost(Constants.REDDIT_LOGIN_URL);
			httppost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			
			HttpParams params = httppost.getParams();
			HttpConnectionParams.setConnectionTimeout(params, 45000);
			HttpConnectionParams.setSoTimeout(params, 45000);
			
			HttpResponse response = client.execute(httppost);
			status = response.getStatusLine().toString();
			if (!status.contains("OK")) {
				throw new HttpException(status);
			}
			
			entity = response.getEntity();
			BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent()));
			String line = in.readLine();
			in.close();
			entity.consumeContent();
			if (StringUtils.isEmpty(line)) {
				throw new HttpException("No content return from Login POST");
			}
			if (Constants.LOGGING) Common.logDLong(TAG, line);
			if (RedditIsFunHttpClientFactory.getCookieStore().getCookies().isEmpty()) {
				throw new HttpException("Failed to login: No cookies");
			}
			
			final JsonFactory jsonFactory = new JsonFactory();
			
			
		} catch (Exception e) {
			mUserError = userError;
			if (entity != null) {
				try {
					entity.consumeContent();
				} catch (Exception e2) {
					if (Constants.LOGGING) Log.e(TAG, "entity.consumeContent()");
				}
			}
			if (Constants.LOGGING) Log.e(TAG, "doLogin()", e);
		}
		return false;
	}

	
}
