package ee.promobox.promoboxandroid;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import ee.promobox.promoboxandroid.util.ToastIntent;


public class MainService extends Service {

    public final static String MAIN_SERVICE_STRING = "MainService ";

    public final static String DEFAULT_SERVER = "http://46.182.31.101:8080"; //"http://api.promobox.ee/";
    public final static String DEFAULT_SERVER_JSON = DEFAULT_SERVER + "/service/device/%s/pull";

    private SharedPreferences sharedPref;

    private String uuid;
    private String audioDevice; // which audio interface is used for output
    private int orientation;
    private int currentFileId;

    private String previousCampaignsJSON = new String();

    private boolean firstTime = true;

    private final AtomicBoolean isDownloading = new AtomicBoolean(false);

    private Campaign currentCampaign;
    private Campaign loadingCampaign;
    private int loadingCampaignProgress;
    private CampaignList campaigns;

    public final static File ROOT = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/promobox/");

    private final IBinder mBinder = new MainServiceBinder();
    private LocalBroadcastManager bManager;
    private DownloadFilesTask dTask;

    @Override
    public void onCreate() {
        Log.i(MAIN_SERVICE_STRING, "onCreate()");
        setSharedPref(PreferenceManager.getDefaultSharedPreferences(this));
        bManager = LocalBroadcastManager.getInstance(this);
        dTask = new DownloadFilesTask(this);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(MAIN_SERVICE_STRING, "Start command");

        setUuid(getSharedPref().getString("uuid", "fail"));
        setOrientation(getSharedPref().getInt("orientation", MainActivity.ORIENTATION_LANDSCAPE));

        checkAndDownloadCampaign();

        if ( firstTime ) {
            Intent mainActivity = new Intent(getBaseContext(), MainActivity.class);

            mainActivity.setAction(Intent.ACTION_MAIN);
            mainActivity.addCategory(Intent.CATEGORY_LAUNCHER);
            mainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);

            getApplication().startActivity(mainActivity);

            firstTime = false;
        }
        return Service.START_NOT_STICKY;
    }

    public void checkAndDownloadCampaign() {
        Log.d(MAIN_SERVICE_STRING, "checkAndDownloadCampaign()");
        try {
            File data = new File(ROOT, "data.json");

            if (data.exists()) {
                String dataString = FileUtils.readFileToString(data);
                JSONObject dataJSON =  new JSONObject(dataString);
                JSONArray campaignsJSON = new JSONArray();

                if (dataJSON.has("campaigns")){
                    campaignsJSON = new JSONObject(dataString).getJSONArray("campaigns");
                }

                if (!previousCampaignsJSON.equals(campaignsJSON.toString())){
                    Log.d(MAIN_SERVICE_STRING, previousCampaignsJSON + "\n" + dataString);
                    previousCampaignsJSON = campaignsJSON.toString();
                    setCampaigns(new CampaignList(campaignsJSON));
                }
                selectNextCampaign();
            }
        } catch (Exception ex) {
            Log.e(MAIN_SERVICE_STRING, ex.getMessage(), ex);
            bManager.sendBroadcast(new ToastIntent(ex.getMessage()));
        }

        if (getUuid() != null) {
            dTask = new DownloadFilesTask(this);
            dTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.format(DEFAULT_SERVER_JSON, getUuid()));
        }

    }

    public boolean isActive() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);

        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;

        return componentInfo.getPackageName().startsWith("ee.promobox.promoboxandroid");

    }

    public Campaign getCurrentCampaign() {
        return currentCampaign;
    }

    public void selectNextCampaign() {
        Log.d(MAIN_SERVICE_STRING, "selectNextCampaign()");
        if(getCampaigns() != null) {
            Date currentDate = new Date();

            // Check for invalid device date.
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            int year = calendar.get(Calendar.YEAR);
            if(year < 2014) {
                if(getCampaigns().size() > 0) {
                    setCurrentCampaign(getCampaigns().get(0));
                    return;
                }
            }

            Campaign campaignToSetCurrent = null;
            int counter = 0;
            for(Campaign camp: getCampaigns()) {
                // Current date between start and end dates of currentCampaign.
                if(camp.hasToBePlayed()) {
                    Log.d(MAIN_SERVICE_STRING, "Date bounds for currentCampaign: " + camp.getCampaignName());
                    campaignToSetCurrent  = camp;
                    counter ++;
                }
            }
            if( counter == 1){
                setCurrentCampaign(campaignToSetCurrent);
                return;
            } else if (counter > 1){
                Log.e(MAIN_SERVICE_STRING, " More than one current campaign");
                bManager.sendBroadcast(new ToastIntent(" Two campaigns int time"));
            }
            setCurrentCampaign(null);
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        getSharedPref().edit().putString("uuid", uuid).commit();
        this.uuid = uuid;
    }

    public int getCurrentFileId() {
        return currentFileId;
    }

    public void setCurrentFileId(int currentFileId) {
        this.currentFileId = currentFileId;
    }

    public String getAudioDevice() {
        return audioDevice;
    }

    public void setAudioDevice(String audioDevice) {
        getSharedPref().edit().putString(MainActivity.AUDIO_DEVICE_PREF, audioDevice).commit();
        this.audioDevice = audioDevice;
    }

    public SharedPreferences getSharedPref() {
        return sharedPref;
    }

    public void setSharedPref(SharedPreferences sharedPref) {
        this.sharedPref = sharedPref;
    }

    public AtomicBoolean getIsDownloading() {
        return isDownloading;
    }

    public void setCurrentCampaign(Campaign currentCampaign) {
        if (this.currentCampaign != null
                && !this.currentCampaign.equals(currentCampaign)){
            Intent update = new Intent(MainActivity.CAMPAIGN_UPDATE);
            bManager.sendBroadcast(update);
        }
        this.currentCampaign = currentCampaign;
    }

    public Campaign getLoadingCampaign() {
        return loadingCampaign;
    }

    public void setLoadingCampaign(Campaign loadingCampaign) {
        this.loadingCampaign = loadingCampaign;
    }

    public int getLoadingCampaignProgress() {
        return loadingCampaignProgress;
    }

    public void setLoadingCampaignProgress(int loadingCampaignProgress) {
        this.loadingCampaignProgress = loadingCampaignProgress;
    }

    public CampaignList getCampaigns() {
        return campaigns;
    }

    public void setCampaigns(CampaignList campaigns) {
        this.campaigns = campaigns;
    }

    public class MainServiceBinder extends Binder {
        MainService getService() {
            return MainService.this;
        }
    }

}
