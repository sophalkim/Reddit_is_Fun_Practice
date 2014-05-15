package ssk.project.Practice.settings;

import ssk.project.Practice.util.Util;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.andrewshu.android.reddit.R;
import com.andrewshu.android.reddit.common.Constants;
import com.andrewshu.android.reddit.mail.EnvelopeService;
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
		return true;
	}

	@Override
	public boolean onPreferenceChange(Preference pref, Object objValue) {
		if (pref.getKey().equals(Constants.PREF_HOMEPAGE)) {
			pref.setSummary((String) objValue);
			return true;
		} else if (pref.getKey().equals(Constants.PREF_THEME)) {
			pref.setSummary(getVisualThemeName((String) objValue));
			return true;
		} else if (pref.getKey().equals(Constants.PREF_TEXT_SIZE)) {
            pref.setSummary(getVisualTextSizeName((String) objValue));
            return true;
    	} else if (pref.getKey().equals(Constants.PREF_ROTATION)) {
            pref.setSummary(getVisualRotationName((String) objValue));
            return true;
        } else if (pref.getKey().equals(Constants.PREF_MAIL_NOTIFICATION_STYLE)) {
        	pref.setSummary(getVisualMailNotificationStyleName((String) objValue));
        	Preference servicePref = findPreference(Constants.PREF_MAIL_NOTIFICATION_SERVICE);
        	if (Constants.PREF_MAIL_NOTIFICATION_SERVICE_OFF.equals(objValue)) {
        		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        		notificationManager.cancel(Constants.NOTIFICATION_HAVE_MAIL);
        		onPreferenceChange(servicePref, Constants.PREF_MAIL_NOTIFICATION_SERVICE_OFF);
        		servicePref.setEnabled(false);
        	} else {
        		if (!servicePref.isEnabled()) {
        			servicePref.setEnabled(true);
        			String servicePrefValue = getPreferenceScreen().getSharedPreferences().getString(Constants.PREF_MAIL_NOTIFICATION_SERVICE, null);
        			if (!Constants.PREF_MAIL_NOTIFICATION_SERVICE_OFF.equals(servicePrefValue)) {
        				onPreferenceChange(servicePref, servicePrefValue);
        			}
        		}
        	}
        	return true;
        } else if (pref.getKey().equals(Constants.PREF_MAIL_NOTIFICATION_SERVICE)) {
        	pref.setSummary(getVisualMailNotificationServiceName((String) objValue));
        	if (Constants.PREF_MAIL_NOTIFICATION_SERVICE_OFF.equals(objValue)) {
        		EnvelopeService.resetAlarm(this, 0);
        		Toast.makeText(this, R.string.mail_notification_unscheduled, Toast.LENGTH_LONG);
        	} else {
        		EnvelopeService.resetAlarm(this, Util.getMillisFromMailNotificationPref((String) objValue));
        		Toast.makeText(this, "Reddit mail will be checked: " + getVisualMailNotificationServiceName((String) objValue), Toast.LENGTH_LONG);
        	}
        	return true;
        }
		return false;
	}

	private CharSequence getVisualThemeName(String enumName) {
		CharSequence[] visualNames = getResources().getTextArray(R.array.pref_theme_choices);
		CharSequence[] enumNames = getResources().getTextArray(R.array.pref_theme_values);
		if (visualNames.length != enumNames.length) {
			return "";
		}
		for (int i = 0; i < enumNames.length; i++) {
			if (enumNames[i].equals(enumName)) {
				return visualNames[i];
			}
		}
		return "";
	}
	
	private CharSequence getVisualTextSizeName(String enumName) {
        CharSequence[] visualNames = getResources().getTextArray(
                R.array.pref_text_size_choices);
        CharSequence[] enumNames = getResources().getTextArray(
                R.array.pref_text_size_values);
        // Sanity check
        if (visualNames.length != enumNames.length) {
            return "";
        }
        for (int i = 0; i < enumNames.length; i++) {
            if (enumNames[i].equals(enumName)) {
                return visualNames[i];
            }
        }
        return "";
    }
    
    private CharSequence getVisualRotationName(String enumName) {
        CharSequence[] visualNames = getResources().getTextArray(
                R.array.pref_rotation_choices);
        CharSequence[] enumNames = getResources().getTextArray(
                R.array.pref_rotation_values);
        // Sanity check
        if (visualNames.length != enumNames.length) {
            return "";
        }
        for (int i = 0; i < enumNames.length; i++) {
            if (enumNames[i].equals(enumName)) {
                return visualNames[i];
            }
        }
        return "";
    }
    
    private CharSequence getVisualMailNotificationStyleName(String enumName) {
        CharSequence[] visualNames = getResources().getTextArray(
                R.array.pref_mail_notification_style_choices);
        CharSequence[] enumNames = getResources().getTextArray(
                R.array.pref_mail_notification_style_values);
        // Sanity check
        if (visualNames.length != enumNames.length) {
            return "";
        }
        for (int i = 0; i < enumNames.length; i++) {
            if (enumNames[i].equals(enumName)) {
                return visualNames[i];
            }
        }
        return "";
    }
    private CharSequence getVisualMailNotificationServiceName(String enumName) {
        CharSequence[] visualNames = getResources().getTextArray(
                R.array.pref_mail_notification_service_choices);
        CharSequence[] enumNames = getResources().getTextArray(
                R.array.pref_mail_notification_service_values);
        // Sanity check
        if (visualNames.length != enumNames.length) {
            return "";
        }
        for (int i = 0; i < enumNames.length; i++) {
            if (enumNames[i].equals(enumName)) {
                return visualNames[i];
            }
        }
        return "";
    }
}
