package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends Activity {

    private final static String AUDIO_DEVICE_PARAM = "audio_devices_out_active";
    public final static  String AUDIO_DEVICE_PREF = "audio_device";

    public final static String CAMPAIGN_UPDATE  = "ee.promobox.promoboxandroid.UPDATE";
    public final static String ACTIVITY_FINISH  = "ee.promobox.promoboxandroid.FINISH";
    public static final String CURRENT_FILE_ID  = "ee.promobox.promoboxandroid.CURRENT_FILE_ID";
    public static final String MAKE_TOAST       = "ee.promobox.promoboxandroid.MAKE_TOAST";
    public final static String APP_START        = "ee.promobox.promoboxandroid.START";

    public final static String MAIN_ACTIVITY_STRING = "MainActivity";

    public final static int RESULT_FINISH_PLAY = 1;
    public final static int RESULT_FINISH_FIRST_START = 2;

    public static final int ORIENTATION_LANDSCAPE = 1;
    public static final int ORIENTATION_PORTRAIT = 2;
    public static final int ORIENTATION_PORTRAIT_EMULATION = 3;


    private MainService mainService;
    private int position;
    private Campaign campaign;
    private boolean mBound = false;

    private void hideSystemUI() {

        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
        );


        View view = findViewById(R.id.main_view);

        view.setOnLongClickListener(new View.OnLongClickListener() {

            public boolean onLongClick(View view) {
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);
                return true;
            }
        });
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);

        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(CAMPAIGN_UPDATE);
        intentFilter.addAction(CURRENT_FILE_ID);
        intentFilter.addAction(MAKE_TOAST);

        bManager.registerReceiver(bReceiver, intentFilter);

        Intent start = new Intent();
        start.setAction(MainActivity.APP_START);
        sendBroadcast(start);

        setAudioDeviceFromPrefs();
    }



    private void startNextFile() {
        if (campaign != null && campaign.getFiles() != null && campaign.getFiles().size() > 0) {
            Log.d(MAIN_ACTIVITY_STRING, "startNextFile() in " + campaign.getCampaignName());

            if (position == campaign.getFiles().size()) {
                position = 0;
                mainService.checkAndDownloadCampaign();
                Log.i(MAIN_ACTIVITY_STRING, "Starting from position 0");
            }

            //mainService.setCurrentFileId(campaign.getFiles().get(position).getId());

            CampaignFileType fileType = null;
            ArrayList<CampaignFile> filePack = new ArrayList<CampaignFile>();

            for (int i = position; i < campaign.getFiles().size(); i++) {
                CampaignFile cFile = campaign.getFiles().get(i);

                if (fileType == null) {
                    fileType = cFile.getType();
                }

                if (cFile.getType() == fileType) {

                    filePack.add(cFile);
                    fileType = cFile.getType();
                    position++;

                } else {
                    break;
                }
            }

            if (campaign.getFiles().size() == 1) {
                campaign.setDelay(60 * 60 * 12);
            }


            if (fileType == CampaignFileType.IMAGE) {

                Intent i = new Intent(this, ImageActivity.class);

                i.putParcelableArrayListExtra("files", filePack);

                i.putExtra("delay", campaign.getDelay());
                i.putExtra("orientation", mainService.getOrientation());

                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivityForResult(i, RESULT_FINISH_PLAY);

                this.overridePendingTransition(0, 0);


            } else if (fileType == CampaignFileType.AUDIO) {

                Intent i = new Intent(this, AudioActivity.class);

                i.putParcelableArrayListExtra("files", filePack);

                i.putExtra("orientation", mainService.getOrientation());

                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivityForResult(i, RESULT_FINISH_PLAY);

            } else if (fileType == CampaignFileType.VIDEO) {

                Intent i = new Intent(this, VideoActivity.class);

                i.putParcelableArrayListExtra("files", filePack);

                i.putExtra("orientation", mainService.getOrientation());

                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivityForResult(i, RESULT_FINISH_PLAY);

                this.overridePendingTransition(0, 0);

            }


        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RESULT_FINISH_PLAY) {
            startNextFile();
        } else if (requestCode == RESULT_FINISH_FIRST_START) {
            try {

                mainService.setUuid(data.getStringExtra("deviceUuid"));
                mainService.checkAndDownloadCampaign();

                startNextFile();

            } catch (Exception ex) {
                Log.e(this.getClass().getName(), ex.getMessage(), ex);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        hideSystemUI();

        Intent intent = new Intent(this, MainService.class);

        startService(intent);

        if (!mBound) {
            bindService(intent, mConnection,
                    Context.BIND_AUTO_CREATE);

            mBound = true;
        }

        if (mainService != null) {
            if (mainService.getOrientation() == MainActivity.ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }

            String audioDevice = mainService.getAudioDevice();
            if(audioDevice != null) {
                setAudioDevice(audioDevice);
            }
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    private void setAudioDeviceFromPrefs() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String audioDevice = sharedPref.getString(AUDIO_DEVICE_PREF,"AUDIO_CODEC");
        setAudioDevice(audioDevice);
    }

    private void setAudioDevice(String audioDevice) {
        AudioManager am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        //am.setParameters("audio_devices_out=AUDIO_CODEC,AUDIO_HDMI,AUDIO_SPDIF"); // for reference.
        if(!audioDevice.equals(am.getParameters(AUDIO_DEVICE_PARAM))) {
            am.setParameters(AUDIO_DEVICE_PARAM + "=" + audioDevice);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }


    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {

            MainService.MainServiceBinder b = (MainService.MainServiceBinder) binder;

            mainService = b.getService();

            campaign = mainService.getCurrentCampaign();

            if (mainService.getUuid() == null || mainService.getUuid().equals("fail")) {
                startActivityForResult(new Intent(MainActivity.this, FirstActivity.class), 2);
            } else {
                startNextFile();
            }

        }

        public void onServiceDisconnected(ComponentName className) {
            mainService = null;
        }
    };

    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        private final String RECEIVER_STRING = MAIN_ACTIVITY_STRING + "BroadcastReceiver";
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(CAMPAIGN_UPDATE)) {
                campaign = mainService.getCurrentCampaign();
                Log.d(RECEIVER_STRING, "CAMPAIGN_UPDATE to " + campaign.getCampaignName());
                position = 0;
                startNextFile();
            } else if (intent.getAction().equals(CURRENT_FILE_ID)) {
                mainService.setCurrentFileId(intent.getExtras().getInt("fileId"));
                Log.d(RECEIVER_STRING, "CURRENT_FILE_ID = " + mainService.getCurrentFileId());
            } else if (intent.getAction().equals(MAKE_TOAST)){
                Log.d(RECEIVER_STRING, "Make TOAST");
                Toast.makeText(getApplicationContext(),intent.getStringExtra("Toast"), Toast.LENGTH_LONG).show();
            }
        }
    };


}
