package ee.promobox.promoboxupdater;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;


public class MainActivity extends ActionBarActivity {
    public static final String TAG        = "MainActivity";

    public static final String PROMOBOX_PACKAGE = "ee.promobox.promoboxandroid";

    public static final String APP_START        = "ee.promobox.promoboxupdater.START";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        if (getIntent().getBooleanExtra("install",false)){
            Log.d(TAG,"install");
            int installedVersion = getInstalledAppVersion();
            int actualVersion = getIntent().getIntExtra("actualVersion",installedVersion);
            if (installApk() && installedVersion != actualVersion){
                tryRunning();
            } else {
                Log.w(TAG, "NOT INSTALLED");
                Toast.makeText(this,"NOT INSTALLED", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            Intent start = new Intent();
            start.setAction(MainActivity.APP_START);
            sendBroadcast(start);
            finish();
        }
    }

    private void tryRunning() {
            Log.d(TAG, "installed");
            final Handler starter = new Handler();
            starter.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent i = getPackageManager().getLaunchIntentForPackage(PROMOBOX_PACKAGE);
                    if ( i != null){
                        startActivity(i);
                        finish();
                    } else {
                        Log.w(TAG, "getLaunchIntentForPackage() is NULL");
                        starter.postDelayed(this,10000);
                    }
                }
            }, 10000);
    }

    /**
     * pm install: installs a package to the system.  Options:
         -l: install the package with FORWARD_LOCK.
         -r: reinstall an exisiting app, keeping its data.
         -t: allow test .apks to be installed.
         -i: specify the installer package name.
         -s: install package on sdcard.
         -f: install package on internal flash.
         -d: allow version code downgrade.
     * @return
     */
    private boolean installApk(){
        File apkFile = new File(MainService.getROOTPath(), UpdaterTask.APK_FILE_NAME);
        Log.d(TAG, "uri = " + apkFile.getAbsolutePath());
        String command = String.format("pm install -r -d %s", apkFile.getAbsolutePath());
        try {
//            Runtime.getRuntime().exec(command).waitFor();
            Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
            return true;
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            return false;
        }
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
            return MainService.VERSION_0;
        }
    }
}
