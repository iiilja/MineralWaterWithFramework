package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ee.promobox.promoboxandroid.data.Campaign;
import ee.promobox.promoboxandroid.data.CampaignFile;
import ee.promobox.promoboxandroid.data.CampaignFileType;
import ee.promobox.promoboxandroid.data.ErrorMessage;
import ee.promobox.promoboxandroid.util.ExceptionHandler;
import ee.promobox.promoboxandroid.util.FragmentPlaybackListener;


public class MainActivity extends Activity implements FragmentPlaybackListener , View.OnLongClickListener{

    private final static String AUDIO_DEVICE_PARAM = "audio_devices_out_active";
    public final static  String AUDIO_DEVICE_PREF = "audio_device";

    public static final String CAMPAIGN_UPDATE  = "ee.promobox.promoboxandroid.UPDATE";
    public static final String MAKE_TOAST       = "ee.promobox.promoboxandroid.MAKE_TOAST";
    public static final String APP_START        = "ee.promobox.promoboxandroid.START";
    public static final String SET_STATUS       = "ee.promobox.promoboxandroid.SET_STATUS";
    public static final String ADD_ERROR_MSG    = "ee.promobox.promoboxandroid.ADD_ERROR_MSG";
    public static final String WRONG_UUID    = "ee.promobox.promoboxandroid.WRONG_UUID";
    public static final String PLAY_SPECIFIC_FILE       = "ee.promobox.promoboxandroid.PLAY_SPECIFIC_FILE";

    public static final String ERROR_MESSAGE       = "Error %d , ( %s )";

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
    private boolean wrongUuid = false;

    private String exceptionHandlerError;

    private boolean mBound = false;

    Fragment mainFragment = new FragmentMain();
    Fragment audioFragment = new FragmentAudio();
    Fragment videoFragment = new FragmentVideo();
    Fragment imageFragment = new FragmentImage();


    private void hideSystemUI() {

        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
        );


        View view = findViewById(R.id.main_view);

        view.setOnLongClickListener(this);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        exceptionHandlerError =  getIntent().getStringExtra("error");

        setContentView(R.layout.activity_main);

        bManager = LocalBroadcastManager.getInstance(this);

        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(CAMPAIGN_UPDATE);
        intentFilter.addAction(MAKE_TOAST);
        intentFilter.addAction(PLAY_SPECIFIC_FILE);
        intentFilter.addAction(SET_STATUS);
        intentFilter.addAction(ADD_ERROR_MSG);
        intentFilter.addAction(WRONG_UUID);

        bManager.registerReceiver(bReceiver, intentFilter);

        getFragmentManager().beginTransaction().add(R.id.main_view, mainFragment).addToBackStack(null).commit();

        Intent start = new Intent();
        start.setAction(MainActivity.APP_START);
        sendBroadcast(start);

        setAudioDeviceFromPrefs();
    }

    private void startNextFile() {
        Log.d(MAIN_ACTIVITY_STRING, "startNextFile()");
        CampaignFile campaignFile = getNextFile(null);
        Fragment fragment = null;
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        CampaignFileType fileType = null;

        if (campaignFile == null){
            fragment = mainFragment;
        } else {
            fileType = campaignFile.getType();
        }

        if (fileType == CampaignFileType.IMAGE) {
            fragment = imageFragment;
            Bundle data = fragment.getArguments();
            if (data != null){
                data.putInt("delay",campaign.getDelay()*1000);
            } else {
                data = new Bundle();
                data.putInt("delay",campaign.getDelay() * 1000);
                fragment.setArguments(data);
            }

        } else if (fileType == CampaignFileType.AUDIO) {
            fragment = audioFragment;

        } else if (fileType == CampaignFileType.VIDEO) {
            fragment = videoFragment;

        }
        if (fragment != null && fragment.isAdded() ){
            //fragment.onPause();
            fragment.onResume();
        } else if ( fragment != null ){
            transaction.replace(R.id.main_view, fragment);
            transaction.addToBackStack(null);
            transaction.commitAllowingStateLoss();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(MAIN_ACTIVITY_STRING," onActivityResult() ,requestCode = " + requestCode);
        if (requestCode == RESULT_FINISH_FIRST_START) {
            try {

                wrongUuid = false;
                if (mainService != null){
                    mainService.setUuid(data.getStringExtra("deviceUuid"));
                    mainService.checkAndDownloadCampaign();
                }


            } catch (Exception ex) {
                Toast.makeText(this, String.format(
                        ERROR_MESSAGE, 31, ex.getClass().getSimpleName()),
                        Toast.LENGTH_LONG).show();
                Log.e(this.getClass().getName(), ex.getMessage(), ex);
                mainService.addError(new ErrorMessage(ex.toString(),ex.getMessage(),ex.getStackTrace()), false);
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
            if (mainService.getOrientation() == ORIENTATION_PORTRAIT) {
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
        if ( wrongUuid ) {
            startActivityForResult(new Intent(MainActivity.this, FirstActivity.class), RESULT_FINISH_FIRST_START);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
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

            if (exceptionHandlerError != null){
                mainService.addError(new ErrorMessage("UncaughtException", exceptionHandlerError, null), true);
            }

            if (campaign == null || !campaign.equals(mainService.getCurrentCampaign())){
                campaign = mainService.getCurrentCampaign();
                campaignWasUpdated(MAIN_ACTIVITY_STRING + " mConnection");
            }

            if (mainService.getUuid() == null || mainService.getUuid().equals("fail")) {
                if (! wrongUuid ){
                    wrongUuid = true;
                    startActivityForResult(new Intent(MainActivity.this, FirstActivity.class), RESULT_FINISH_FIRST_START);
                }
            }

        }

        public void onServiceDisconnected(ComponentName className) {
            mainService = null;
        }
    };

    private void updateStatus( String status ){
        TextView textView = (TextView) findViewById(R.id.main_activity_status);
        if (textView != null){
            textView.setText(status);
        }
    }

    public void addError(ErrorMessage message, boolean broadcastNow){
        mainService.addError(message, broadcastNow);
    }

    public void makeToast(String toast){
        boolean silentMode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("silent_mode", false);
        if (!silentMode){
            Toast.makeText(this,toast ,Toast.LENGTH_LONG).show();
        }
    }

    public int getOrientation(){
        if (mainService == null) {
            return ORIENTATION_LANDSCAPE;
        }
        return mainService.getOrientation();
    }

    private void campaignWasUpdated(String tag) {
        Log.d(tag, "CAMPAIGN_UPDATE to " + (campaign != null ? campaign.getCampaignName() : "NONE"));
        updateStatus( campaign != null ? campaign.getCampaignName() : NO_ACTIVE_CAMPAIGN);
        position = 0;
        startNextFile();
    }

    @Override
    public void onPlaybackStop() {
        Log.w(MAIN_ACTIVITY_STRING, "onPlaybackStop");
        startNextFile();
    }


    @Override
    public boolean onLongClick(View view) {
        Intent i = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(i);
        return true;
    }

    public CampaignFile getNextFile(CampaignFileType fileTypeNeeded) {
        CampaignFile campaignFile = null;
        boolean playingSpecificFile = false;

        if (campaign != null && nextSpecificFile == null &&
                campaign.getFiles() != null && campaign.getFiles().size() > 0) {

            if (position == campaign.getFiles().size()) {
                position = 0;
                Log.i(MAIN_ACTIVITY_STRING, "Starting from position 0");
            }

            if (campaign.getFiles().size() == 1) {
                campaign.setDelay(60 * 60 * 12);
            }

            campaignFile = campaign.getFiles().get(position);

        } else if (nextSpecificFile != null) {
            campaignFile = nextSpecificFile;
            playingSpecificFile = true;
            if (fileTypeNeeded != null){
                nextSpecificFile = null;
            }

        } else {
            if (campaign != null ){
                updateStatus("No files to play in " + campaign.getCampaignName());
            } else {
                updateStatus(NO_ACTIVE_CAMPAIGN);
            }
            Log.i(MAIN_ACTIVITY_STRING, "CAMPAIGN = NULL");
        }
        CampaignFileType fileType = campaignFile != null ? campaignFile.getType() : null;
        boolean fileTypeOK = fileTypeNeeded != null && fileTypeNeeded.equals(fileType);
        if (fileTypeOK && !playingSpecificFile){
            setCurrentFileId(campaignFile.getId());
            position ++;
        } else if ( fileTypeNeeded != null && !fileTypeNeeded.equals(fileType)){
            Log.d(MAIN_ACTIVITY_STRING, " file type not as needed");
            campaignFile = null;
        }

        return campaignFile;
    }

    private void setCurrentFileId(int currentFileId) {
        Log.d(MAIN_ACTIVITY_STRING, "Current file id : " +currentFileId);
        mainService.setCurrentFileId(currentFileId);
    }


    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        private final String RECEIVER_STRING = MAIN_ACTIVITY_STRING + "BroadcastReceiver";
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(CAMPAIGN_UPDATE)) {

                if (mainService == null ||
                        (campaign != null && campaign.equals(mainService.getCurrentCampaign()))) return;

                campaign = mainService.getCurrentCampaign();
                mainService.setActivityReceivedUpdate(true);

                campaignWasUpdated(RECEIVER_STRING);

            } else if (action.equals(MAKE_TOAST)){

                String toastString = intent.getStringExtra("Toast");
                Log.d(RECEIVER_STRING, "Make TOAST :" + toastString);
                makeToast(toastString);

            } else if (action.equals(PLAY_SPECIFIC_FILE)){

                nextSpecificFile = intent.getParcelableExtra("campaignFile");
                Log.d(RECEIVER_STRING, "PLAY_SPECIFIC_FILE with id " + nextSpecificFile.getId());
                startNextFile();
            } else if ( action.equals(SET_STATUS)){

                String status = intent.getStringExtra("status");
                Log.d(RECEIVER_STRING, SET_STATUS +" "+ status);
                updateStatus(intent.getStringExtra("status"));
            } else if ( action.equals(ADD_ERROR_MSG)){

                ErrorMessage message = intent.getParcelableExtra("message");
                Log.d(RECEIVER_STRING, "Got error MSG " + message.getMessage());
                addError(message, false);
            } else if ( action.equals(WRONG_UUID)){

                Log.d(RECEIVER_STRING, WRONG_UUID );
                if ( !wrongUuid ) {
                    wrongUuid = true;
                    startActivityForResult(new Intent(MainActivity.this, FirstActivity.class), RESULT_FINISH_FIRST_START);
                }
            }
        }
    };
}
