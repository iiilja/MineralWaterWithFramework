package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;

import java.util.ArrayList;

import ee.promobox.promoboxandroid.data.Display;
import ee.promobox.promoboxandroid.util.geom.Rectangle;
import ee.promobox.promoboxandroid.util.geom.TriangleEquilateral;


public class SettingsActivity extends PreferenceActivity {
    private static String TAG = "SettingsActivity";

    private ArrayList<Display> displays;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        displays = getIntent().getParcelableArrayListExtra("displays");
//        PreferenceManager.getDefaultSharedPreferences(this).edit()
//                .putString("version" , BuildConfig.VERSION_CODE + "").apply();
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
        if ( displays != null ) {
            Bundle arguments = new Bundle();
            arguments.putParcelableArrayList("displays", displays);
            fragment.setArguments(arguments);
        } else {
            Log.w(TAG, "displays is null");
        }

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment)
                .commit();
    }

    public ArrayList<Display> getDisplays() {
        return displays;
    }

    @Override
    public boolean onIsMultiPane() {
        return false;
    }


    public static class GeneralPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.pref_general);

            findPreference("version").setSummary( BuildConfig.VERSION_CODE + "");

            ListPreference list = (ListPreference) findPreference("monitor_id");
            if (getArguments() != null && getArguments().getParcelableArrayList("displays") != null) {
                ArrayList<Display> displays = getArguments().getParcelableArrayList("displays");
                CharSequence[] entries = new CharSequence[displays.size()];
                CharSequence[] entryValues = new CharSequence[displays.size()];
                for (int i = 0; i < displays.size(); i++) {
                    Display d = displays.get(i);
                    entryValues[i] = d.getId() + "";

                    int width = (int) TriangleEquilateral.getLineLength(d.getPoints()[0], d.getPoints()[1]);
                    int height = (int) TriangleEquilateral.getLineLength(d.getPoints()[1], d.getPoints()[2]);
                    entries[i] = d.getId() + " - (" + width + " X " + height + ")";
                }
                list.setEntries(entries);
                list.setEntryValues(entryValues);
            } else {
                list.setEnabled(false);
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

            findPreference("uuid").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    String uuid = (String) o;
                    Log.d(TAG,o.toString());
                    Log.d(TAG, uuid);

                    return !uuid.equals("");
                }
            });



        }
    }

}
