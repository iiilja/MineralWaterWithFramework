package ee.promobox.promoboxupdater;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.IOException;


public class MainActivity extends ActionBarActivity {
    public static final String TAG        = "MainActivity";

    public static final String APP_START        = "ee.promobox.promoboxupdater.START";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        boolean installed = false;
        if (getIntent().getBooleanExtra("install",false)){
            Log.d(TAG,"install");
            installed = installApk();
        } else {
            Intent start = new Intent();
            start.setAction(MainActivity.APP_START);
            sendBroadcast(start);
        }
        finish();
        if (installed) {
            Log.d(TAG, "installed");
            final Handler starter = new Handler();
            starter.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent i = getPackageManager().getLaunchIntentForPackage("ee.promobox.promoboxandroid");
                    if ( i != null){
                        startActivity(i);
                    } else {
                        starter.postDelayed(this,10000);
                    }
                }
            }, 10000);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
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
}
