package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Toast;

import java.io.File;


public class MainActivity extends Activity {

    public final static String CAMPAIGN_UPDATE = "ee.promobox.promoboxandroid.UPDATE";
    public final static String ACTIVITY_FINISH = "ee,promobox.promoboxandroid.FINISH";

    private MainService mainService;
    private int position;
    private Campaign campaign;

    private void hideSystemUI() {

        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
        );
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        hideSystemUI();

        Intent service = new Intent(this, MainService.class);

        startService(service);

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);

        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(CAMPAIGN_UPDATE);

        bManager.registerReceiver(bReceiver, intentFilter);

    }

    private void startNextFile() {
        if (campaign != null) {

            CampaignFile file = campaign.getFiles().get(position);

            position++;

            if (position == campaign.getFiles().size()) {
                position = 0;
                mainService.checkAndDownloadCampaign();
            }

            if (file.getType() == CampaignFileType.IMAGE) {

                Intent i = new Intent(this, ImageActivity.class);

                File dFile = new File(campaign.getRoot(), file.getName());

                if (dFile.exists()) {

                    i.putExtra("source", new File(campaign.getRoot(), file.getName()).getAbsolutePath());

                    startActivityForResult(i, 1);
                } else {
                    startNextFile();
                }

            } else if (file.getType() == CampaignFileType.AUDIO) {

                Intent i = new Intent(this, AudioActivity.class);

                File dFile = new File(campaign.getRoot(), file.getName());

                if (dFile.exists()) {

                    i.putExtra("source", new File(campaign.getRoot(), file.getName()).getAbsolutePath());

                    startActivityForResult(i, 1);
                } else {
                    startNextFile();
                }
            }


        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {

            if (resultCode == RESULT_OK) {
                startNextFile();
            }

            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        hideSystemUI();

        Intent intent = new Intent(this, MainService.class);

        bindService(intent, mConnection,
                Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onPause() {
        super.onPause();
        //unbindService(mConnection);
    }


    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {

            MainService.MyBinder b = (MainService.MyBinder) binder;

            mainService = b.getService();

            campaign = mainService.getCampaign();

            Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT)
                    .show();

            startNextFile();

        }

        public void onServiceDisconnected(ComponentName className) {
            mainService = null;
        }
    };

    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(CAMPAIGN_UPDATE)) {
              campaign = mainService.getCampaign();
              position = 0;
              startNextFile();
            }
        }
    };


}
