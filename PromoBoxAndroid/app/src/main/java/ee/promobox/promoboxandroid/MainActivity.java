package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import java.io.File;


public class MainActivity extends Activity {

    public final static String CAMPAIGN_UPDATE = "ee.promobox.promoboxandroid.UPDATE";
    public final static String ACTIVITY_FINISH = "ee,promobox.promoboxandroid.FINISH";

    private MainService mainService;
    private int position;
    private Campaign campaign;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

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

                i.putExtra("source", new File(campaign.getRoot(), file.getName()).getAbsolutePath());

                startActivityForResult(i, 1);

            } else if (file.getType() == CampaignFileType.AUDIO) {

                Intent i = new Intent(this, AudioActivity.class);

                i.putExtra("source", new File(campaign.getRoot(), file.getName()).getAbsolutePath());

                startActivityForResult(i, 1);
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
