package ee.promobox.promoboxandroid.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ilja on 6.05.2015.
 */
public class Settings implements Parcelable{

    public static final String MONITOR_ID_PREF = "monitor_id";
    public static final String SILENT_MODE_PREF = "silent_mode";
    public static final String SYNC_FREQUENCY_PREF = "sync_frequency";
    public static final String UUID_PREF = "uuid";

    private int monitorId = -1;
    private boolean silentMode = false;
    private int syncFrequency = 30;
    private String uuid = "fail";

    public Settings(){

    }

    public Settings(Parcel in) {
        monitorId = in.readInt();
        silentMode = in.readByte() != 0;
        syncFrequency = in.readInt();
        uuid = in.readString();
    }

    public Settings(int monitorId, boolean silentMode, int syncFrequency, String uuid) {
        this.monitorId = monitorId;
        this.silentMode = silentMode;
        this.syncFrequency = syncFrequency;
        this.uuid = uuid;
    }

    public JSONObject getJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(MONITOR_ID_PREF,monitorId);
        json.put(SILENT_MODE_PREF,silentMode);
        json.put(SYNC_FREQUENCY_PREF,syncFrequency);
        json.put(UUID_PREF,uuid);

        return json;
    }

    public int getMonitorId() {
        return monitorId;
    }

    public void setMonitorId(int monitorId) {
        this.monitorId = monitorId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


    public boolean getSilentMode() {
        return silentMode;
    }

    public void setSilentMode(boolean silentMode) {
        this.silentMode = silentMode;
    }

    public int getSyncFrequency() {
        return syncFrequency;
    }

    public void setSyncFrequency(int syncFrequency) {
        this.syncFrequency = syncFrequency;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dst, int flags) {
        dst.writeInt(monitorId);
        dst.writeByte((byte) (silentMode ? 1 : 0));
        dst.writeInt(syncFrequency);
        dst.writeString(uuid);
    }

    public static final Parcelable.Creator<Settings> CREATOR = new Parcelable.Creator<Settings>() {
        public Settings createFromParcel(Parcel in) {
            return new Settings(in);
        }

        public Settings[] newArray(int size) {
            return new Settings[size];
        }
    };

    public static Settings copy(Settings settings){
        return new Settings(settings.getMonitorId(),settings.silentMode,
                settings.syncFrequency,settings.getUuid());
    }

    @Override
    public boolean equals(Object o) {
        if (Settings.class != o.getClass()) return false;

        Settings settings = (Settings) o;
        boolean equals = settings.monitorId == monitorId;
        equals &= settings.silentMode == silentMode;
        equals &= settings.syncFrequency == syncFrequency;
        equals &= settings.uuid.equals(uuid);
        return equals;
    }
}
