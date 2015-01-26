package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.app.ActivityManager;
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
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    private final static String AUDIO_DEVICE_PARAM = "audio_devices_out_active";
    public final static  String AUDIO_DEVICE_PREF = "audio_device";

    public static final String CAMPAIGN_UPDATE  = "ee.promobox.promoboxandroid.UPDATE";
    public static final String ACTIVITY_FINISH  = "ee.promobox.promoboxandroid.FINISH";
    public static final String CURRENT_FILE_ID  = "ee.promobox.promoboxandroid.CURRENT_FILE_ID";
    public static final String MAKE_TOAST       = "ee.promobox.promoboxandroid.MAKE_TOAST";
    public static final String NO_NETWORK       = "ee.promobox.promoboxandroid.NO_NETWORK";
    public static final String APP_START        = "ee.promobox.promoboxandroid.START";
    public static final String SET_STATUS       = "ee.promobox.promoboxandroid.SET_STATUS";
    public static final String PLAY_SPECIFIC_FILE       = "ee.promobox.promoboxandroid.PLAY_SPECIFIC_FILE";

    private static final String NO_ACTIVE_CAMPAIGN       = "no active campaign";

    public final static String MAIN_ACTIVITY_STRING = "MainActivity";

    public final static int RESULT_FINISH_PLAY = 1;
    public final static int RESULT_FINISH_FIRST_START = 2;

    public static final int ORIENTATION_LANDSCAPE = 1;
    public static final int ORIENTATION_PORTRAIT = 2;
    public static final int ORIENTATION_PORTRAIT_EMULATION = 3;

    LocalBroadcastManager bManager;

    private MainService mainService;
    private int position;
    private Campaign campaign;

    private CampaignFile nextSpecificFile = null;
    private boolean nextSpecificFilePlaying = false;

    private boolean mBound = false;
    private boolean active = true;

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

        bManager = LocalBroadcastManager.getInstance(this);

        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(CAMPAIGN_UPDATE);
        intentFilter.addAction(CURRENT_FILE_ID);
        intentFilter.addAction(MAKE_TOAST);
        intentFilter.addAction(NO_NETWORK);
        intentFilter.addAction(PLAY_SPECIFIC_FILE);
        intentFilter.addAction(SET_STATUS);

        bManager.registerReceiver(bReceiver, intentFilter);

        Intent start = new Intent();
        start.setAction(MainActivity.APP_START);
        sendBroadcast(start);

        setAudioDeviceFromPrefs();
    }



    private void startNextFile() {
        if (!active){
            Log.e(MAIN_ACTIVITY_STRING, "AM NOT ACTIVE");
            return;
        }
        if (campaign != null && nextSpecificFile == null &&
                campaign.getFiles() != null && campaign.getFiles().size() > 0) {

            Log.d(MAIN_ACTIVITY_STRING, "startNextFile() in " + campaign.getCampaignName());
            active = false;
            if (nextSpecificFilePlaying) nextSpecificFilePlaying = false;


            if (position == campaign.getFiles().size()) {
                position = 0;
                Log.i(MAIN_ACTIVITY_STRING, "Starting from position 0");
            }

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

            startPlayingActivity(fileType,filePack);


        } else if (nextSpecificFile != null) {
            ArrayList<CampaignFile> filePack = new ArrayList<CampaignFile>();
            filePack.add(nextSpecificFile);
            startPlayingActivity(nextSpecificFile.getType(), filePack);
            nextSpecificFile = null;
            nextSpecificFilePlaying = true;
        } else {
                Log.i(MAIN_ACTIVITY_STRING, "CAMPAIGN = NULL");
        }
    }

    private void startPlayingActivity(CampaignFileType fileType, ArrayList<CampaignFile> filePack){
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(MAIN_ACTIVITY_STRING," onActivityResult() ,requestCode = " + requestCode);
        if (requestCode == RESULT_FINISH_PLAY) {

////            startNextFile();

        } else if (requestCode == RESULT_FINISH_FIRST_START) {
            try {

                mainService.setUuid(data.getStringExtra("deviceUuid"));
                mainService.checkAndDownloadCampaign();

//                startNextFile();

            } catch (Exception ex) {
                Log.e(this.getClass().getName(), ex.getMessage(), ex);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(MAIN_ACTIVITY_STRING, "onResume");

        hideSystemUI();


        if (!mBound) {
            Intent intent = new Intent(this, MainService.class);
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
        active = true;
        startNextFile();
    }

    @Override
    protected void onPause() {
        super.onPause();
        active = false;
        Log.d(MAIN_ACTIVITY_STRING, "onPause");
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

            Log.d(MAIN_ACTIVITY_STRING, "onServiceConnected");
            MainService.MainServiceBinder b = (MainService.MainServiceBinder) binder;

            mainService = b.getService();

            campaign = mainService.getCurrentCampaign();

            if (mainService.getUuid() == null || mainService.getUuid().equals("fail")) {
                startActivityForResult(new Intent(MainActivity.this, FirstActivity.class), RESULT_FINISH_FIRST_START);
            }

        }

        public void onServiceDisconnected(ComponentName className) {
            mainService = null;
        }
    };

    public boolean activityIsActive(String className) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);

        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;

        String openClassName = componentInfo.getClassName();
        Log.d(MAIN_ACTIVITY_STRING, "openClassName = " + openClassName + " className  =" +className);
        return openClassName.equals(className) ;

    }

    private void updateStatus( String status ){
        TextView textView = (TextView) findViewById(R.id.main_activity_status);
        textView.setText(status);
    }

    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        private final String RECEIVER_STRING = MAIN_ACTIVITY_STRING + "BroadcastReceiver";
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(CAMPAIGN_UPDATE)) {

                if (mainService == null){
                    return;
                }
                campaign = mainService.getCurrentCampaign();
                Log.d(RECEIVER_STRING, "CAMPAIGN_UPDATE to " + (campaign != null ? campaign.getCampaignName() : "NONE"));
                updateStatus( campaign != null ? campaign.getCampaignName() : NO_ACTIVE_CAMPAIGN);
                position = 0;
                if (active){
                    Log.d(RECEIVER_STRING, MAIN_ACTIVITY_STRING + " active, start next file from receiver");
                    startNextFile();
                } else {
                    Log.d(RECEIVER_STRING, "Broadcasting to finish active activity");
                    bManager.sendBroadcast(new Intent(ACTIVITY_FINISH));
                }

            } else if (action.equals(CURRENT_FILE_ID)) {

                mainService.setCurrentFileId(intent.getExtras().getInt("fileId"));
                Log.d(RECEIVER_STRING, "CURRENT_FILE_ID = " + mainService.getCurrentFileId());
            } else if (action.equals(MAKE_TOAST)){

                Log.d(RECEIVER_STRING, "Make TOAST");
                Toast.makeText(getApplicationContext(),intent.getStringExtra("Toast"), Toast.LENGTH_LONG).show();

            } else if (action.equals(NO_NETWORK)){

                Log.d(RECEIVER_STRING, "NO NETWORK");
                try {
                    new NoNetworkDialog().show(getFragmentManager(),"NO_NETWORK");
                } catch (IllegalStateException ex){
                }

            } else if (action.equals(PLAY_SPECIFIC_FILE)){

                nextSpecificFile = intent.getParcelableExtra("campaignFile");
                Log.d(RECEIVER_STRING, "PLAY_SPECIFIC_FILE with id " + nextSpecificFile.getId());
                if (active){
                    startNextFile();
                } else {
                    if (campaign != null){
                        if ( !nextSpecificFilePlaying ) {
                            int fileId = mainService.getCurrentFileId();
                            position = campaign.getCampaignFilePositionById(fileId);
                            position ++;
                        }
                    }
                    bManager.sendBroadcast(new Intent(ACTIVITY_FINISH));
                }
            } else if ( action.equals(SET_STATUS)){

                String status = intent.getStringExtra("status");
                Log.d(RECEIVER_STRING, SET_STATUS +" "+ status);
                updateStatus(intent.getStringExtra("status"));
            }
        }
    };


}
