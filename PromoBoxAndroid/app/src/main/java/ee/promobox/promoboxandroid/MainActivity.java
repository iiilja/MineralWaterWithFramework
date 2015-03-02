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
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import ee.promobox.promoboxandroid.data.Campaign;
import ee.promobox.promoboxandroid.data.CampaignFile;
import ee.promobox.promoboxandroid.data.CampaignFileType;
import ee.promobox.promoboxandroid.data.ErrorMessage;
import ee.promobox.promoboxandroid.util.ExceptionHandler;
import ee.promobox.promoboxandroid.util.FragmentPlaybackListener;
import ee.promobox.promoboxandroid.util.InternetConnectionUtil;
import ee.promobox.promoboxandroid.util.StatusEnum;
import ee.promobox.promoboxandroid.util.VideoWallMasterListener;
import ee.promobox.promoboxandroid.util.udp_multicasting.MessageReceivedListener;
import ee.promobox.promoboxandroid.util.udp_multicasting.UDPMessenger;
import ee.promobox.promoboxandroid.util.udp_multicasting.messages.MultiCastMessage;
import ee.promobox.promoboxandroid.util.udp_multicasting.messages.PlayMessage;
import ee.promobox.promoboxandroid.util.udp_multicasting.messages.PrepareMessage;
import ee.promobox.promoboxandroid.widgets.FragmentVideoWall;


public class MainActivity extends Activity implements FragmentPlaybackListener , View.OnLongClickListener, MessageReceivedListener, VideoWallMasterListener{

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

    private static final String NO_ACTIVE_CAMPAIGN = "NO ACTIVE CAMPAIGN AT THE MOMENT";

    public final static String TAG = "MainActivity";

    public final static int RESULT_FINISH_PLAY = 1;
    public final static int RESULT_FINISH_FIRST_START = 2;
    public static final int RESULT_FINISH_NETWORK_SETTING = 3;

    public static final int ORIENTATION_LANDSCAPE = 1;
    public static final int ORIENTATION_PORTRAIT = 2;
    public static final int ORIENTATION_PORTRAIT_EMULATION = 3;
    private LocalBroadcastManager bManager;

    private MainService mainService;
    private int position;
    private Campaign campaign;

    private CampaignFile nextSpecificFile = null;

    private boolean wrongUuid = false;

    private String exceptionHandlerError;

    private boolean mBound = false;

    FragmentMain mainFragment = new FragmentMain();
    Fragment audioFragment = new FragmentAudio();
    Fragment videoFragment = new FragmentVideo();
    Fragment imageFragment = new FragmentImage();
    Fragment currentFragment;

    private UDPMessenger udpMessenger;
    private boolean videoWall = false;
    private boolean master = true;


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

        getFragmentManager().beginTransaction().add(R.id.main_view, mainFragment).addToBackStack(mainFragment.toString()).commit();
        currentFragment = mainFragment;
        Intent start = new Intent();
        start.setAction(MainActivity.APP_START);
        sendBroadcast(start);

        setAudioDeviceFromPrefs();

        Handler messageHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                MultiCastMessage message = (MultiCastMessage) msg.obj;
                switch (message.getType()) {
                    case MultiCastMessage.PLAY:
                        onPlayMessageReceived((PlayMessage) message);
                        break;
                    case MultiCastMessage.PREPARE:
                        onPrepareMessageReceived((PrepareMessage) message);
                        break;
                }
            }
        };

        udpMessenger = new UDPMessenger(getBaseContext(), messageHandler, "Lala", 47654) ;
        udpMessenger.startMessageReceiver();
    }

    private void startNextFile() {
        Log.d(TAG, "startNextFile()");
        CampaignFile campaignFile = getNextFile(null);
        Fragment fragment;
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        CampaignFileType fileType = campaignFile != null ? campaignFile.getType() : null;

        fragment = getFragmentByFileType(fileType);

        Bundle data = fragment.getArguments();

        int delay = campaign != null ? campaign.getDelay() : 0;
        if (data != null){
            data.putInt("delay",delay*100);
        } else if (!fragment.equals(currentFragment) ){
            data = new Bundle();
            data.putInt("delay",delay * 100);
            fragment.setArguments(data);
        }

        if (fragment.equals(currentFragment) ){
            Log.w(TAG, "Current fragment stays (onPause, onResume)");
            fragment.onPause();
            fragment.onResume();
        } else {
            transaction.replace(R.id.main_view, fragment);
            transaction.addToBackStack(fragment.toString());
            transaction.commit();
        }
        currentFragment = fragment;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG," onActivityResult() ,requestCode = " + requestCode);
        if (requestCode == RESULT_FINISH_FIRST_START) {
            try {
                wrongUuid = false;
                if (mainService != null && data != null){
                    mainService.setUuid(data.getStringExtra("deviceUuid"));
                    mainService.checkAndDownloadCampaign();
                } else if (data == null) {
                    wrongUuid = true;
                    startActivityForResult(new Intent(MainActivity.this, FirstActivity.class), RESULT_FINISH_FIRST_START);
                }


            } catch (Exception ex) {
                Toast.makeText(this, String.format(
                        ERROR_MESSAGE, 31, ex.getClass().getSimpleName()),
                        Toast.LENGTH_LONG).show();
                Log.e(this.getClass().getName(), ex.getMessage(), ex);
                mainService.addError(new ErrorMessage(ex.toString(),ex.getMessage(),ex.getStackTrace()), false);
            }
        } else if (requestCode == RESULT_FINISH_NETWORK_SETTING) {
            wrongUuid = false;
            mainService.checkAndDownloadCampaign();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

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
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");

        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onBackPressed() {
        Log.w(TAG, "Back pressed, do nothing");
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




    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {

            Log.d(TAG, "onServiceConnected");
            MainService.MainServiceBinder b = (MainService.MainServiceBinder) binder;

            mainService = b.getService();

            if (exceptionHandlerError != null){
                mainService.addError(new ErrorMessage("UncaughtException", exceptionHandlerError, null), true);
            }

            if (campaign == null || !campaign.equals(mainService.getCurrentCampaign())){
                campaign = mainService.getCurrentCampaign();
                if (! videoWall ){
                    imageFragment = new FragmentWallImage();
                    videoWall = true;
                }
                campaignWasUpdated(TAG + " mConnection");
            }

            if (mainService.getUuid() == null || mainService.getUuid().equals("fail")) {
                if (! wrongUuid ){
                    wrongUuid = true;
                    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    if ( !InternetConnectionUtil.isNetworkConnected(cm) ) {
                        startActivityForResult(new Intent(MainActivity.this, WifiActivity.class), RESULT_FINISH_NETWORK_SETTING);
                    } else {
                        startActivityForResult(new Intent(MainActivity.this, FirstActivity.class), RESULT_FINISH_FIRST_START);
                    }
                }
            }

        }

        public void onServiceDisconnected(ComponentName className) {
            mainService = null;
        }
    };

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

    private Fragment getFragmentByFileType(CampaignFileType fileType){
        if (fileType == null ){
            return mainFragment;
        }
        switch ( fileType ){
            case IMAGE:
                return imageFragment;
            case AUDIO:
                return audioFragment;
            case VIDEO:
                return videoFragment;
            default:
                return mainFragment;
        }
    }

    private void campaignWasUpdated(String tag) {
        Log.d(tag, "CAMPAIGN_UPDATE to " + (campaign != null ? campaign.getCampaignName() : "NONE"));
        StatusEnum statusEnum = campaign != null ? StatusEnum.NO_ACTIVE_CAMPAIGN : null;
        mainFragment.updateStatus(statusEnum, campaign != null ? campaign.getCampaignName() : NO_ACTIVE_CAMPAIGN);
        position = 0;
        startNextFile();
    }

    @Override
    public void onPlaybackStop() {
        Log.w(TAG, "onPlaybackStop");
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
                Log.i(TAG, "Starting from position 0");
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
                mainFragment.updateStatus(StatusEnum.NO_FILES,"No files to play in " + campaign.getCampaignName());
                mainService.setCurrentFileId(0);
            } else {
                mainFragment.updateStatus(StatusEnum.NO_ACTIVE_CAMPAIGN,NO_ACTIVE_CAMPAIGN);
            }
            Log.i(TAG, "CAMPAIGN = NULL");
        }
        CampaignFileType fileType = campaignFile != null ? campaignFile.getType() : null;
        boolean fileTypeOK = fileTypeNeeded != null && fileTypeNeeded.equals(fileType);
        if (fileTypeOK && !playingSpecificFile){
            setCurrentFileId(campaignFile.getId());
            position ++;
        } else if ( fileTypeNeeded != null && !fileTypeNeeded.equals(fileType)){
            Log.d(TAG, " file type not as needed");
            campaignFile = null;
        }

        return campaignFile;
    }

    public boolean isMaster() {
        return master;
    }

    public boolean isVideoWall() {
        return videoWall;
    }

    public void setPreviousFilePosition(){
        position = position - 2;
        if (position < 0 && campaign != null && campaign.getFiles() != null) {
            position = campaign.getFiles().size() + position;
            if (position < 0) {
                position = 0;
            }
        }
    }

    private void setCurrentFileId(int currentFileId) {
        Log.d(TAG, "Current file id : " +currentFileId);
        mainService.setCurrentFileId(currentFileId);
    }


    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        private final String RECEIVER_STRING = TAG + "BroadcastReceiver";
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case CAMPAIGN_UPDATE:

                    if (mainService == null ||
                            (campaign != null && campaign.equals(mainService.getCurrentCampaign())))
                        return;

                    campaign = mainService.getCurrentCampaign();
                    mainService.setActivityReceivedUpdate(true);

                    campaignWasUpdated(RECEIVER_STRING);

                    break;
                case MAKE_TOAST:

                    String toastString = intent.getStringExtra("Toast");
                    Log.d(RECEIVER_STRING, "Make TOAST :" + toastString);
                    makeToast(toastString);

                    break;
                case PLAY_SPECIFIC_FILE:

                    nextSpecificFile = intent.getParcelableExtra("campaignFile");
                    Log.d(RECEIVER_STRING, "PLAY_SPECIFIC_FILE with id " + nextSpecificFile.getId());
                    startNextFile();
                    break;
                case SET_STATUS:

                    String status = intent.getStringExtra("status");
                    StatusEnum statusEnum = (StatusEnum) intent.getSerializableExtra("statusEnum");
                    Log.d(RECEIVER_STRING, SET_STATUS + " " + status + " statusEnum = " + statusEnum.toString());
                    mainFragment.updateStatus(statusEnum, intent.getStringExtra("status"));
                    break;
                case ADD_ERROR_MSG:

                    ErrorMessage message = intent.getParcelableExtra("message");
                    Log.d(RECEIVER_STRING, "Got error MSG " + message.getMessage());
                    addError(message, false);
                    break;
                case WRONG_UUID:

                    Log.d(RECEIVER_STRING, WRONG_UUID);
                    if (!wrongUuid) {
                        wrongUuid = true;
                        startActivityForResult(new Intent(MainActivity.this, FirstActivity.class), RESULT_FINISH_FIRST_START);
                    }
                    break;
            }
        }
    };

    @Override
    public void onPlayMessageReceived(PlayMessage message) {
        Log.d(TAG, "onPlayMessageReceived");
        if (!videoWall){
            onPrepareMessageReceived(new PrepareMessage("1111",message.getCampaignId(),message.getFileId()));
        }
        if (mainService == null || mainService.getCampaigns() == null) return;
        CampaignFile campaignFile = mainService.getCampaigns().getCampaignWithId(message.getCampaignId()).getFileById(message.getFileId());
        FragmentVideoWall fragmentByType = (FragmentVideoWall) getFragmentByFileType(campaignFile.getType());
        if ( !fragmentByType.equals(currentFragment) ) {
            startNextFile();
        }
        fragmentByType.playFile(campaignFile,message.getFrameId());

    }

    @Override
    public void onPrepareMessageReceived(PrepareMessage message) {
        Log.d(TAG, "onPrepareMessageReceived");
        if (!videoWall){
            imageFragment = new FragmentWallImage();
            videoWall = true;
        }
        if (mainService == null || mainService.getCampaigns() == null) return;
        CampaignFile campaignFile = mainService.getCampaigns().getCampaignWithId(message.getCampaignId()).getFileById(message.getFileId());
        FragmentVideoWall fragment = (FragmentVideoWall) getFragmentByFileType(campaignFile.getType());
        fragment.prepareFile(campaignFile);
    }


    @Override
    public void onFileNotPrepared() {
        udpMessenger.sendMessage(new PrepareMessage("1111",campaign.getCampaignId(),getNextFile(null).getId()));
    }

    @Override
    public void onFileStartedPlaying(int fileId, long frameId) {
        udpMessenger.sendMessage(new PlayMessage("1111",campaign.getCampaignId(),fileId,frameId));
    }
}
