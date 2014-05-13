package ssk.project.Practice.login;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.widget.EditText;

import com.andrewshu.android.reddit.R;
import com.andrewshu.android.reddit.settings.RedditSettings;

public abstract class LoginDialog extends Dialog {
	private final Activity mActivity;
	private final RedditSettings mSettings;
	private final EditText loginUsernameInput;
	private final EditText loginPasswordInput;

	public LoginDialog(final Activity activity, RedditSettings settings, boolean finishActivityIfCanceled) {
		super(activity, settings.getDialogTheme());
		mActivity = activity;
		mSettings = settings;
		
		setContentView(R.layout.login_dialog);
		setTitle("Login to reddit.com");
		if (finishActivityIfCanceled) {
			setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface d) {
					if (!mSettings.isLoggedIn()) {
						mActivity.setResult(Activity.RESULT_CANCELED);
						mActivity.finish();
					}
				}
			});
		}
		
		
	}

}
