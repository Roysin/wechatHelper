package org.roysin.wechathelper;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

/**
 * Created by Zangyakui on 2016/3/22.
 */
public class SettingsPreference extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener{
    private static final String TAG = "SettingsPreference";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings_layout);
        Preference readPref = getPreferenceManager().findPreference("switch_reading");
        readPref.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.i(TAG, "onPreferenceChange key = " + preference.getKey() + " newValue: " + newValue);
        if (preference.getKey().equals("switch_reading")){

            if ((boolean) newValue){
               BackToReadingService.startSelf(getActivity());
            }else{
               BackToReadingService.stop(getActivity());
            }
        }
        return true;
    }
}
