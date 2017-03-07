package com.chen;

import android.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.*;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import com.chen.common.Utilities;

public class GpsSettingsActivity extends PreferenceActivity
{

    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
/*

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean useImperial = prefs.getBoolean("useImperial", false);

        EditTextPreference distanceBeforeLogging = (EditTextPreference) findPreference("distance_before_logging");

        if (useImperial)
        {
            distanceBeforeLogging.setDialogTitle(R.string.settings_distance_in_feet);
            distanceBeforeLogging.getEditText().setHint(R.string.settings_enter_feet);
        }
        else
        {
            distanceBeforeLogging.setDialogTitle(R.string.settings_distance_in_meters);
            distanceBeforeLogging.getEditText().setHint(R.string.settings_enter_meters);
        }

        CheckBoxPreference imperialCheckBox = (CheckBoxPreference) findPreference("useImperial");
        imperialCheckBox.setOnPreferenceChangeListener(new ImperialPreferenceChangeListener(prefs, distanceBeforeLogging));
*/
        Preference enableDisablePref = findPreference("enableDisableGps");
        enableDisablePref.setOnPreferenceClickListener(new AndroidLocationPreferenceClickListener());

    }


    private final Runnable updateResults = new Runnable()
    {
        public void run()
        {
            finish();

            startActivity(getIntent());
        }

    };


    /**
     * Opens the Android Location preferences screen
     */
    private class AndroidLocationPreferenceClickListener implements OnPreferenceClickListener
    {
        public boolean onPreferenceClick(Preference preference)
        {
            startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
            return true;
        }
    }


}
