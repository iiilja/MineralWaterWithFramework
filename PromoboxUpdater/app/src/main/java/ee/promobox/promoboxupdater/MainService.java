package ee.promobox.promoboxupdater;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;



public class MainService extends Service {

    public final static String TAG = "MainService ";

    public static final int VERSION_0 = 0;
    public static final String VERSION = "version";

//    public final static String DEFAULT_SERVER = "http://46.182.31.101:8080"; //"http://api.promobox.ee/";
        public final static String DEFAULT_SERVER = "http://46.182.30.93:8080"; // production
    public final static String DEFAULT_SERVER_VERSION = DEFAULT_SERVER + "/service/version";

    private static File ROOT = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/promobox/");

    private final IBinder mBinder = new MainServiceBinder();
    private LocalBroadcastManager bManager;
    private UpdaterTask task;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate()");
        bManager = LocalBroadcastManager.getInstance(this);

        checkExternalSD();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "Start command");
        task = new UpdaterTask(this);

        task.execute();

        return Service.START_NOT_STICKY;
    }


    private void checkExternalSD() {
        File file = new File("/mnt/external_sd");
        if (file.exists() && file.listFiles() != null && file.listFiles().length > 1) {
            Log.d(TAG, "/mnt/external_sd EXISTS");
            ROOT = new File(file.getPath() + "/promobox/");
        }
        if (!ROOT.exists()) {
            try {
                FileUtils.forceMkdir(ROOT);
            } catch (IOException ex) {
                Log.e(TAG, ex.getMessage());
            }
        }
        Log.d(TAG, " ROOT  = " + ROOT.getPath());
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }


    public int getInstalledAppVersion() {
        int version = 0;
        Context otherAppsContext;
        try {
            otherAppsContext = createPackageContext("ee.promobox.promoboxandroid", 0);
            PackageInfo pInfo = null;

            pInfo = getPackageManager().getPackageInfo(otherAppsContext.getPackageName(), 0);
            Log.d(TAG, "VERSION name = " + pInfo.versionName + " code = " + pInfo.versionCode);
            version = pInfo.versionCode;

            return version;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG,e.getMessage());
            return VERSION_0;
        }
    }

    public class MainServiceBinder extends Binder {
        MainService getService() {
            return MainService.this;
        }
    }

    public static String getROOTPath() {
        return ROOT.getPath();
    }

    public File getROOT() {
        return ROOT;
    }
}

