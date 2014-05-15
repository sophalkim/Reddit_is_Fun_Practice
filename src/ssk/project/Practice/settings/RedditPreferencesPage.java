package ssk.project.Practice.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.andrewshu.android.reddit.R;
import com.andrewshu.android.reddit.common.Constants;
import com.andrewshu.android.reddit.settings.RedditSettings;

public class RedditPreferencesPage extends PreferenceActivity implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.reddit_preferences);
		
		Preference e;
		
		e = findPreference(Constants.PREF_HOMEPAGE);
		e.setOnPreferenceChangeListener(this);
		e.setSummary(getPreferenceScreen().getSharedPreferences().getString(Constants.PREF_HOMEPAGE, null));
		
		e = findPreference(Constants.PREF_THEME);
        e.setOnPreferenceChangeListener(this);
        e.setSummary(getVisualThemeName(
        		getPreferenceScreen().getSharedPreferences()
                .getString(Constants.PREF_THEME, null)));
        
        e = findPreference(Constants.PREF_TEXT_SIZE);
        e.setOnPreferenceChangeListener(this);
        e.setSummary(getVisualTextSizeName(
        		getPreferenceScreen().getSharedPreferences()
                .getString(Constants.PREF_TEXT_SIZE, null)));
        
        e = findPreference(Constants.PREF_ROTATION);
        e.setOnPreferenceChangeListener(this);
        e.setSummary(getVisualRotationName(
        		getPreferenceScreen().getSharedPreferences()
                .getString(Constants.PREF_ROTATION, null)));
        
        e = findPreference(Constants.PREF_MAIL_NOTIFICATION_STYLE);
        e.setOnPreferenceChangeListener(this);
        e.setSummary(getVisualMailNotificationStyleName(
        		getPreferenceScreen().getSharedPreferences()
        		.getString(Constants.PREF_MAIL_NOTIFICATION_STYLE, null)));
        
        e = findPreference(Constants.PREF_MAIL_NOTIFICATION_SERVICE);
        e.setOnPreferenceChangeListener(this);
        e.setSummary(getVisualMailNotificationServiceName(
        		getPreferenceScreen().getSharedPreferences()
        		.getString(Constants.PREF_MAIL_NOTIFICATION_SERVICE, null)));
        
        if (getPreferenceScreen().getSharedPreferences().getString(Constants.PREF_MAIL_NOTIFICATION_STYLE, Constants.PREF_MAIL_NOTIFICATION_STYLE_OFF)
        		.equals(Constants.PREF_MAIL_NOTIFICATION_STYLE_OFF)) {
        	e.setEnabled(false);
        }
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		setRequestedOrientation(RedditSettings.Rotation.valueOf(prefs.getString(Constants.PREF_ROTATION, Constants.PREF_ROTATION_UNSPECIFIED)));
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		return false;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		return false;
	}

	private CharSequence getVisualThemeName(String enumName) {
		CharSequence[] visualNames = getResources().getTextArray(R.array.pref_theme_choices);
		CharSequence[] enumNames = getResources().getTextArray(R.array.pref_theme_values);
	}
}
