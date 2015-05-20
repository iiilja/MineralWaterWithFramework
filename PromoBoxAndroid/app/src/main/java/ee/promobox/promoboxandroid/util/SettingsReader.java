package ee.promobox.promoboxandroid.util;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import ee.promobox.promoboxandroid.data.Settings;

/**
 * Created by ilja on 5.05.2015.
 */
public class SettingsReader {

    private static final String TAG = "SettingsReader";
    private static final String SETTINGS_FILE_NAME = "settings";

    Settings settings = new Settings();

    private File settingsFile = null;

    public SettingsReader(File ROOT){
        settingsFile = new File(ROOT, SETTINGS_FILE_NAME);

        JSONObject json;
        json = readJSONFromFile(settingsFile);

        if (json != null){
            handleJSON(json);
        }
    }

    public Settings getSettings() {
        return settings;
    }

    private JSONObject readJSONFromFile(File file) {
        if (!file.exists()) return null;

        JSONObject json;
        try {
            json = new JSONObject(FileUtils.readFileToString(file));
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            json = null;
        }
        return json;
    }

    private void handleJSON(JSONObject json) {
        if (json == null) return;
        try {
            if (json.has(Settings.MONITOR_ID_PREF)){
                settings.setMonitorId(json.getInt(Settings.MONITOR_ID_PREF));
            }
            if (json.has(Settings.SILENT_MODE_PREF)){
                settings.setSilentMode(json.getBoolean(Settings.SILENT_MODE_PREF));
            }
            if (json.has(Settings.SYNC_FREQUENCY_PREF)){
                settings.setSyncFrequency(json.getInt(Settings.SYNC_FREQUENCY_PREF));
            }
            if (json.has(Settings.UUID_PREF)){
                settings.setUuid(json.getString(Settings.UUID_PREF));
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    public int getMonitorId() {
        return settings.getMonitorId();
    }

    public void setMonitorId(int monitorId) throws SettingsSavingException {
        Settings copy = Settings.copy(settings);
        copy.setMonitorId(monitorId);
        try {
            writeToFile(copy);
            settings = copy;
        } catch (IOException | JSONException e) {
            throw new SettingsSavingException(e);
        }
    }

    public String getUuid() {
        return settings.getUuid();
    }

    public void setUuid(String uuid) throws SettingsSavingException {
        Settings copy = Settings.copy(settings);
        copy.setUuid(uuid);
        try {
            writeToFile(copy);
            settings = copy;
        } catch (IOException | JSONException e) {
            throw new SettingsSavingException(e);
        }
    }

    public void setValuesMigrate(int monitorId, boolean silentMode, int syncFrequency, String uuid) throws SettingsSavingException {
        if (settingsFile.exists()) return;

        setValues(monitorId, silentMode, syncFrequency, uuid);
    }

    public void setValues(int monitorId, boolean silentMode, int syncFrequency, String uuid) throws SettingsSavingException {
        Settings newSettings = new Settings(monitorId, silentMode, syncFrequency, uuid);
        try {
            writeToFile(newSettings);
            settings = newSettings;
        } catch (IOException | JSONException e) {
            throw new SettingsSavingException(e);
        }
    }

    public void setSettings(Settings settings) throws SettingsSavingException {
        try {
            writeToFile(settings);
            this.settings = settings;
        } catch (IOException | JSONException e) {
            throw new SettingsSavingException(e);
        }
    }

    private void writeToFile(Settings settings) throws IOException, JSONException {
        FileUtils.writeStringToFile(settingsFile, settings.getJSON().toString());
    }
}
