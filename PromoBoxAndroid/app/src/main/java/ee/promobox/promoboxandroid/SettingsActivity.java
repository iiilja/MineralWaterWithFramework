package ee.promobox.promoboxandroid;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;

import java.util.ArrayList;

import ee.promobox.promoboxandroid.data.Display;
import ee.promobox.promoboxandroid.data.Settings;


public class SettingsActivity extends PreferenceActivity {
    private static String TAG = "SettingsActivity";

    private ArrayList<Display> displays;
    private Settings settings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        displays = getIntent().getParcelableArrayListExtra("displays");
        settings = getIntent().getParcelableExtra("settings");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        GeneralPreferenceFragment fragment = new GeneralPreferenceFragment();
        Bundle arguments = new Bundle();
        if ( displays != null ) {
            arguments.putParcelableArrayList("displays", displays);
        } else {
            Log.w(TAG, "displays is null");
        }
        arguments.putParcelable("settings", settings);
        Log.d(TAG,"UUID in settings is " + settings.getUuid());

        fragment.setArguments(arguments);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment)
                .commit();
    }

    @Override
    public boolean onIsMultiPane() {
        return false;
    }


    public static class GeneralPreferenceFragment extends PreferenceFragment {

        Settings settings;
        Settings settingsCopy;

        @Override
        public void onPause() {
            if (!settings.equals(settingsCopy)){
                Intent settingsChange = new Intent(MainActivity.SETTINGS_CHANGE);
                settingsChange.putExtra("settings", settingsCopy);
                getActivity().sendBroadcast(settingsChange);
            }
            super.onPause();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);

            settings = getArguments().getParcelable("settings");
            settingsCopy = Settings.copy(settings);

            addPreferencesFromResource(R.xml.pref_general);

            findPreference("version").setSummary( BuildConfig.VERSION_CODE + "");

            ListPreference displayPreference = (ListPreference) findPreference(Settings.MONITOR_ID_PREF);
            displayPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        settingsCopy.setMonitorId(Integer.parseInt((String) newValue));
                    } catch (ClassCastException e){
                        e.printStackTrace();
                    }
                    return true;
                }
            });
            if (getArguments() != null && getArguments().getParcelableArrayList("displays") != null) {
                ArrayList<Display> displays = getArguments().getParcelableArrayList("displays");
                CharSequence[] entries = new CharSequence[displays.size()];
                CharSequence[] entryValues = new CharSequence[displays.size()];
                for (int i = 0; i < displays.size(); i++) {
                    Display d = displays.get(i);
                    entryValues[i] = d.getId() + "";
                    entries[i] = d.getId() + " - [" + d.getPoints()[0] + "; " + d.getPoints()[2] + "]";
                }
                displayPreference.setEntries(entries);
                displayPreference.setEntryValues(entryValues);
            } else {
                displayPreference.setEnabled(false);
            }



            findPreference("close").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Log.i("SettingsActivity", "Close app");

                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    return true;
                }
            });

            EditTextPreference uuidPreference = (EditTextPreference) findPreference(Settings.UUID_PREF);
            uuidPreference.setText(settings.getUuid());
            uuidPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Log.d(TAG, "onUUIDPreference changed");
                    String uuid = (String) o;
                    boolean ok = !uuid.equals("");
                    if (ok) {
                        settingsCopy.setUuid(uuid);
                    }
                    return ok;
                }
            });

            CheckBoxPreference silentModePreference = (CheckBoxPreference) findPreference(Settings.SILENT_MODE_PREF);
            silentModePreference.setChecked(settings.getSilentMode());
            silentModePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean silent = (boolean) newValue;
                    settingsCopy.setSilentMode(silent);
                    return true;
                }
            });

            ListPreference syncFrequencyPreference = (ListPreference) findPreference(Settings.SYNC_FREQUENCY_PREF);
            syncFrequencyPreference.setValue(settings.getSyncFrequency() + "");
            syncFrequencyPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    settingsCopy.setSyncFrequency(Integer.parseInt((String) newValue));
                    return true;
                }
            });
        }
    }

}
