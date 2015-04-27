package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import ee.promobox.promoboxandroid.data.Campaign;
import ee.promobox.promoboxandroid.data.CampaignList;
import ee.promobox.promoboxandroid.data.CampaignMultiple;
import ee.promobox.promoboxandroid.data.Display;
import ee.promobox.promoboxandroid.data.DisplayArrayList;
import ee.promobox.promoboxandroid.data.ErrorMessage;
import ee.promobox.promoboxandroid.data.ErrorMessageArray;
import ee.promobox.promoboxandroid.intents.ToastIntent;
import ee.promobox.promoboxandroid.util.ExceptionHandler;
import ee.promobox.promoboxandroid.util.MainServiceTimerTask;


public class MainService extends Service {

    public final static String TAG = "MainService ";

//    public final static String DEFAULT_SERVER = "http://46.182.31.101:8080"; //"http://api.promobox.ee/";
    public final static String DEFAULT_SERVER = "http://46.182.30.93:8080"; // production
    public final static String DEFAULT_SERVER_JSON = DEFAULT_SERVER + "/service/device/%s/pull";

    private SharedPreferences sharedPref;

    private String uuid;
    private String audioDevice; // which audio interface is used for output
    private int orientation;
    private int currentFileId;

    private Date lastWifiRestartDt;
    private int wifiRestartCounter;
    private Campaign loadingCampaign;
    private double loadingCampaignProgress;


    // Video Wall
    private DisplayArrayList displays;
    private int wallHeight;
    private int wallWidth;
    private boolean videoWall;

    private boolean firstStart = true; // To read DATA.JSON only on first start of service
    private boolean closedNormally = false;
    private boolean firstStartWatchDog = true;
    private boolean activityReceivedUpdate = false; // Sometimes mainActivity receiver starts after this broadcasts
    private Date lastScheduledCallDate = new Date();

    private String previousCampaignsJSON = "";

    private final AtomicBoolean isDownloading = new AtomicBoolean(false);

    private Long timeDifference;

    private Campaign currentCampaign;
    private CampaignList campaigns;
    private ErrorMessageArray errors = new ErrorMessageArray();

    private File ROOT = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/promobox/");

//    private LocalBroadcastManager bManager;
    private DownloadFilesTask dTask;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate()");
        setSharedPref(PreferenceManager.getDefaultSharedPreferences(this));
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this, false, getUuid()));
//        bManager = LocalBroadcastManager.getInstance(this);
        dTask = new DownloadFilesTask(this);

        checkExternalSD();
        setCampaignsFromJSONFile();

        Timer timer = new Timer();
        TimerTask serviceTimer = new MainServiceTimerTask(this);
        timer.scheduleAtFixedRate(serviceTimer, 0, 30000);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        boolean startMainActivity       =  intent == null || intent.getBooleanExtra("startMainActivity",false);
        boolean startedFromMainActivity =  intent == null || intent.getBooleanExtra("startedFromMainActivity",false);
        boolean serviceAlarm =  intent == null || intent.getBooleanExtra("serviceAlarm",false);

//        This os not much needed, just in case if something happens with !isOnTop && !closedNormally this will help
//        For example if user closed (onPause) activity and app died while was closedNormally
        if (serviceAlarm ){
            Date previous = new Date(lastScheduledCallDate.getTime() + MyScheduleReceiver.REPEAT_TIME + 10000 );
            lastScheduledCallDate = new Date();
            if (previous.after(new Date())){
                return Service.START_STICKY;
            } else {
                Log.w(TAG, "Probably mainActivity died, trying to resurrect it");
                startMainActivity = true;
            }
        }

        Log.i(TAG, "Start command");

        setUuid(getSharedPref().getString("uuid", "fail"));
        setOrientation(getSharedPref().getInt("orientation", MainActivity.ORIENTATION_LANDSCAPE));

        boolean isOnTop = getOnTopComponentInfo().getPackageName().startsWith(getPackageName());
        if ( startMainActivity
//                When application killed by user
                || !startedFromMainActivity && firstStartWatchDog && !isOnTop
//                When main process died
                || !isOnTop && !closedNormally) {
            if (!startedFromMainActivity && firstStartWatchDog){
                Log.w("WatchDog", "Activity and service started from alarm");
            } else if (!isOnTop && !closedNormally ) {
                Log.w(TAG, "Activity was not closed normally, starting it");
            }
            startMainActivity();
        }
        firstStartWatchDog = false;

        checkAndDownloadCampaign();
        return Service.START_STICKY;
    }

    public void checkAndDownloadCampaign() {
        Log.d(TAG, "checkAndDownloadCampaign()");
        try {
            selectNextCampaign();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);
            sendBroadcast(new ToastIntent(String.format(MainActivity.ERROR_MESSAGE, 61, ex.getClass().getSimpleName())));
            addError(new ErrorMessage(ex.toString(), ex.getMessage(), ex.getStackTrace()), false);

        }
        if (getUuid() != null) {
            dTask = new DownloadFilesTask(this);
            dTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.format(DEFAULT_SERVER_JSON, getUuid()));
        }

    }

    public Campaign getCurrentCampaign() {
        return currentCampaign;
    }

    public void selectNextCampaign() {
        Log.d(TAG, "selectNextCampaign()");
        if(getCampaigns() != null) {
            Date currentDate = getCurrentDate();

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
            ArrayList<Campaign> campaignsToSetCurrent = new ArrayList<>();
            for(Campaign camp: getCampaigns()) {
                // Current date between start and end dates of currentCampaign.
                if(camp.hasToBePlayed(getCurrentDate())) {
                    Log.d(TAG, "Date bounds for currentCampaign: " + camp.getCampaignName());
                    campaignsToSetCurrent.add(camp);
                }
            }
            if( campaignsToSetCurrent.size() == 1){
                setCurrentCampaign(campaignsToSetCurrent.get(0));
                return;
            } else if ( campaignsToSetCurrent.size() > 1){
                Log.w(TAG, " More than one current campaign");
                setCurrentCampaign(new CampaignMultiple(campaignsToSetCurrent));
                return;
            }
            setCurrentCampaign(null);
        }
    }
    
    public void startMainActivity(){
        Intent mainActivity = new Intent(getBaseContext(), MainActivity.class);

        mainActivity.setAction(Intent.ACTION_MAIN);
        mainActivity.addCategory(Intent.CATEGORY_LAUNCHER);
        mainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);

        getApplication().startActivity(mainActivity);
    }

    private void setCampaignsFromJSONFile(){
        try {
            File data = new File(ROOT, "data.json");

            if (data.exists() && firstStart) {
                firstStart = false;
                String dataString = FileUtils.readFileToString(data);
                JSONObject dataJSON = new JSONObject();
                try {
                    dataJSON = new JSONObject(dataString);
                }  catch (JSONException ex ){
                    Log.e(TAG, "Can not read JSON : " + dataString);
                    sendBroadcast(new ToastIntent(String.format(MainActivity.ERROR_MESSAGE, 62, ex.getClass().getSimpleName())));
                    errors.addError(new ErrorMessage(JSONException.class.getSimpleName(),dataString,ex.getStackTrace()));
                }
                JSONArray campaignsJSON = new JSONArray();

                if (dataJSON.has("campaigns")){
                    campaignsJSON = new JSONObject(dataString).getJSONArray("campaigns");
                }

                if (!previousCampaignsJSON.equals(campaignsJSON.toString())){
                    Log.d(TAG, previousCampaignsJSON + "\n" + dataString);
                    previousCampaignsJSON = campaignsJSON.toString();
                    setCampaigns(new CampaignList(campaignsJSON, ROOT.getPath()));
                }
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.getMessage(), ex);
            sendBroadcast(new ToastIntent(String.format(MainActivity.ERROR_MESSAGE, 62, ex.getClass().getSimpleName())));
            addError(new ErrorMessage(ex), false);
        }
    }

    private void checkExternalSD(){
        File file = new File("/mnt/external_sd");
        if ( file.exists() && file.listFiles() != null && file.listFiles().length > 1){
            Log.d(TAG, "/mnt/external_sd EXISTS");
            ROOT = new File(file.getPath() +  "/promobox/");
        }
        if (!ROOT.exists()){
            try {
                FileUtils.forceMkdir(ROOT);
            } catch (IOException ex) {
                Log.e(TAG, ex.getMessage());
                sendBroadcast(new ToastIntent(String.format(MainActivity.ERROR_MESSAGE, 63, ex.getClass().getSimpleName())));
                addError(new ErrorMessage(ex.toString(), ex.getMessage(), ex.getStackTrace()), false);
            }
        }
        Log.d(TAG, " ROOT  = " + ROOT.getPath());
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    private AIDLInterface.Stub mBinder = new AIDLInterface.Stub() {
        @Override
        public void addError(ErrorMessage errorMessage, boolean broadcastNow) throws RemoteException {
            MainService.this.addError(errorMessage,broadcastNow);
        }

        @Override
        public void setActivityReceivedUpdate(boolean activityReceivedUpdate) throws RemoteException {
            MainService.this.setActivityReceivedUpdate(activityReceivedUpdate);
        }

        @Override
        public Campaign getCampaignWithId(int campaignId) throws RemoteException {
            if (campaigns != null){
                for (Campaign campaign : campaigns){
                    if (campaign.getCampaignId() == campaignId){
                        return campaign;
                    }
                }
            }
            return null;
        }

        @Override
        public boolean isVideoWall() throws RemoteException {
            return MainService.this.isVideoWall();
        }

        @Override
        public int getWallWidth() throws RemoteException {
            return MainService.this.getWallWidth();
        }

        @Override
        public int getWallHeight() throws RemoteException {
            return MainService.this.getWallHeight();
        }

        @Override
        public String getAudioDevice() throws RemoteException {
            return MainService.this.getAudioDevice();
        }

        @Override
        public void setCurrentFileId(int id) throws RemoteException {
            MainService.this.setCurrentFileId(id);
        }

        @Override
        public int getCurrentFileId() throws RemoteException {
            return MainService.this.getCurrentFileId();
        }

        @Override
        public void setUuid(String uuid) throws RemoteException {
            MainService.this.setUuid(uuid);
        }

        @Override
        public String getUuid() throws RemoteException {
            return MainService.this.getUuid();
        }

        @Override
        public int getOrientation() throws RemoteException {
            return MainService.this.getOrientation();
        }

        @Override
        public Campaign getCurrentCampaign() throws RemoteException {
            return MainService.this.getCurrentCampaign();
        }

        @Override
        public void checkAndDownloadCampaign() throws RemoteException {
            MainService.this.checkAndDownloadCampaign();
        }

        @Override
        public List<Display> getDisplays() throws RemoteException {
            return MainService.this.getDisplays();
        }

        @Override
        public void setClosedNormally(boolean closedNormally) throws RemoteException {
            MainService.this.closedNormally = closedNormally;
        }
    };

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public String getUuid() {
        if (uuid == null){
            return getSharedPref().getString("uuid", "fail");
        }
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

    private void setCurrentCampaign(Campaign currentCampaign) {
        // Broadcasting only if campaign is really updated or new.
        // Or if MainActivity receiver did not start as fast as this
        if (!activityReceivedUpdate
                || this.currentCampaign == null && currentCampaign != null
                || this.currentCampaign != null && !this.currentCampaign.equals(currentCampaign)
                || this.currentCampaign != null &&
                    (this.currentCampaign.getUpdateDate() < currentCampaign.getUpdateDate())){
            Log.d(TAG, " UPDATING CURRENT CAMPAIGN FROM setCurrentCampaign");
            setActivityReceivedUpdate(false);
            this.currentCampaign = currentCampaign;
            Intent update = new Intent(MainActivity.CAMPAIGN_UPDATE);
            sendBroadcast(update);
        }
        else {
            this.currentCampaign = currentCampaign;
        }
        if (currentCampaign == null){
            setCurrentFileId(0);
        }
    }

    public boolean setDisplays(DisplayArrayList displays) {
        if (!displays.equals(this.displays)){
            this.displays = displays;
            return true;
        }
        return false;
    }

    public DisplayArrayList getDisplays() {
        return displays;
    }

    public int getWallHeight() {
        return wallHeight;
    }

    public boolean setWallHeight(int wallHeight) {
        if (wallHeight != this.wallHeight) {
            this.wallHeight = wallHeight;
            return true;
        }
        return false;
    }

    public int getWallWidth() {
        return wallWidth;
    }

    public boolean setWallWidth(int wallWidth) {
        if (wallWidth != this.wallWidth) {
            this.wallWidth = wallWidth;
            return true;
        }
        return false;
    }

    public boolean setVideoWall(boolean videoWall) {
        if (this.videoWall != videoWall){
            this.videoWall = videoWall;
            return true;
        }
        return false;
    }

    public boolean isVideoWall() {
        Log.d(TAG, "ASKED IF IS VIDEO WALL - " + videoWall);
        return videoWall;
    }

    public ComponentName getOnTopComponentInfo(){
        ActivityManager am = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        return taskInfo.get(0).topActivity;
    }

    public Campaign getLoadingCampaign() {
        return loadingCampaign;
    }

    public void setLoadingCampaign(Campaign loadingCampaign) {
        this.loadingCampaign = loadingCampaign;
    }

    public double getLoadingCampaignProgress() {
        return loadingCampaignProgress;
    }

    public void setLoadingCampaignProgress(double loadingCampaignProgress) {
        this.loadingCampaignProgress = loadingCampaignProgress;
    }

    public CampaignList getCampaigns() {
        return campaigns;
    }

    public void setCampaigns(CampaignList campaigns) {
        this.campaigns = campaigns;
    }

    public void setWifiRestartCounter(int attemptsNumber) {
        this.wifiRestartCounter = attemptsNumber;
    }

    public int getWifiRestartCounter() {
        return wifiRestartCounter;
    }

    public void setCurrentDate( Date currentDate){
        timeDifference = System.currentTimeMillis() - currentDate.getTime();
        Log.w(TAG, "TIME HAS BEEN SET , difference with server is " + timeDifference);
    }

    public Date getCurrentDate(){
        if (timeDifference != null){
            return new Date(System.currentTimeMillis() - timeDifference);
        } else {
            return new Date();
        }
    }

    public String getROOTPath() {
        return ROOT.getPath();
    }

    public File getROOT() {
        return ROOT;
    }

    public Date getLastWifiRestartDt() {
        return lastWifiRestartDt;
    }

    public void setLastWifiRestartDt(Date lastWifiRestartDt) {
        this.lastWifiRestartDt = lastWifiRestartDt;
    }

    public void setActivityReceivedUpdate(boolean activityReceivedUpdate) {
        this.activityReceivedUpdate = activityReceivedUpdate;
    }

    public ErrorMessageArray getErrors() {
        return errors;
    }

    public void setErrors(ErrorMessageArray errors) {
        this.errors = errors;
    }

    public void addError(ErrorMessage msg, boolean broadcastNow){
        Log.d(TAG,"error added");
        try {
            errors.addError(msg);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            sendBroadcast(new ToastIntent(String.format(MainActivity.ERROR_MESSAGE, 64, e.getClass().getSimpleName())));
            try {
                broadcastNow = true;
                errors.addError(new ErrorMessage(e));
            } catch (JSONException e2){
                Log.e(TAG, e.getMessage());
                sendBroadcast(new ToastIntent(String.format(MainActivity.ERROR_MESSAGE, 65, e.getClass().getSimpleName())));
            }
        }
        if (broadcastNow) {
            dTask = new DownloadFilesTask(this);
            dTask.setOnlySendData(true);
            dTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.format(DEFAULT_SERVER_JSON, getUuid()));
        }
    }
}
