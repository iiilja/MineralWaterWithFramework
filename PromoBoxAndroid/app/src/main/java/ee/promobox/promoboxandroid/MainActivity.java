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
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import ee.promobox.promoboxandroid.data.Campaign;
import ee.promobox.promoboxandroid.data.CampaignFile;
import ee.promobox.promoboxandroid.data.CampaignFileType;
import ee.promobox.promoboxandroid.data.Display;
import ee.promobox.promoboxandroid.data.DisplayArrayList;
import ee.promobox.promoboxandroid.data.ErrorMessage;
import ee.promobox.promoboxandroid.interfaces.FragmentPlaybackListener;
import ee.promobox.promoboxandroid.interfaces.VideoWallMasterListener;
import ee.promobox.promoboxandroid.util.ExceptionHandler;
import ee.promobox.promoboxandroid.util.InternetConnectionUtil;
import ee.promobox.promoboxandroid.util.StatusEnum;
import ee.promobox.promoboxandroid.util.udp_multicasting.JGroupsMessenger;
import ee.promobox.promoboxandroid.util.udp_multicasting.MessageReceivedListener;
import ee.promobox.promoboxandroid.util.udp_multicasting.messages.MultiCastMessage;
import ee.promobox.promoboxandroid.util.udp_multicasting.messages.PlayMessage;
import ee.promobox.promoboxandroid.util.udp_multicasting.messages.PrepareMessage;
import ee.promobox.promoboxandroid.widgets.FragmentVideoWall;


public class MainActivity extends Activity implements FragmentPlaybackListener, View.OnLongClickListener, MessageReceivedListener, VideoWallMasterListener {

    private final static String AUDIO_DEVICE_PARAM = "audio_devices_out_active";
    public final static String AUDIO_DEVICE_PREF = "audio_device";

    public static final String CAMPAIGN_UPDATE = "ee.promobox.promoboxandroid.UPDATE";
    public static final String MAKE_TOAST = "ee.promobox.promoboxandroid.MAKE_TOAST";
    public static final String APP_START = "ee.promobox.promoboxandroid.START";
    public static final String UI_RESURRECT = "ee.promobox.promoboxandroid.RESURRECT";
    public static final String SET_STATUS = "ee.promobox.promoboxandroid.SET_STATUS";
    public static final String ADD_ERROR_MSG = "ee.promobox.promoboxandroid.ADD_ERROR_MSG";
    public static final String WRONG_UUID = "ee.promobox.promoboxandroid.WRONG_UUID";
    public static final String SETTINGS_UUID_CHANGE = "ee.promobox.promoboxandroid.SETTINGS_UUID_CHANGE";
    public static final String PLAY_SPECIFIC_FILE = "ee.promobox.promoboxandroid.PLAY_SPECIFIC_FILE";
    public static final String WALL_UPDATE = "ee.promobox.promoboxandroid.WALL_UPDATE";

    public static final String ERROR_MESSAGE = "Error %d , ( %s )";

    private static final String NO_ACTIVE_CAMPAIGN = "NO ACTIVE CAMPAIGN AT THE MOMENT";

    public final static String TAG = "MainActivity";

    public final static int RESULT_FINISH_PLAY = 1;
    public final static int RESULT_FINISH_FIRST_START = 2;
    public static final int RESULT_FINISH_NETWORK_SETTING = 3;

    public static final int ORIENTATION_LANDSCAPE = 1;
    public static final int ORIENTATION_PORTRAIT = 2;
    public static final int ORIENTATION_PORTRAIT_EMULATION = 3;

    private AIDLInterface mainService;
    private Campaign campaign;

    private CampaignFile nextSpecificFile = null;

    private boolean wrongUuid = false;

    private String exceptionHandlerError;

    private boolean mBound = false;

    FragmentMain mainFragment = new FragmentMain();
    Fragment audioFragment = new FragmentAudio();
    Fragment videoFragment = new FragmentVideo();
    Fragment imageFragment = new FragmentImage();
    Fragment webFragment = new FragmentWeb();
    Fragment currentFragment;

    //    private UDPMessenger udpMessenger;
    private JGroupsMessenger jGroupsMessenger;
    private boolean videoWall = false;
    private boolean master = false;


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

        String uuid = PreferenceManager.getDefaultSharedPreferences(this).getString("uuid", "fail");
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this, true, uuid));
        exceptionHandlerError = getIntent().getStringExtra("error");

        setContentView(R.layout.activity_main);

        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(CAMPAIGN_UPDATE);
        intentFilter.addAction(MAKE_TOAST);
        intentFilter.addAction(PLAY_SPECIFIC_FILE);
        intentFilter.addAction(SET_STATUS);
        intentFilter.addAction(ADD_ERROR_MSG);
        intentFilter.addAction(WRONG_UUID);
        intentFilter.addAction(SETTINGS_UUID_CHANGE);
        intentFilter.addAction(WALL_UPDATE);

        this.getBaseContext().registerReceiver(bReceiver, intentFilter);
//        bManager.registerReceiver(bReceiver, intentFilter);

        getFragmentManager().beginTransaction().add(R.id.main_view, mainFragment).addToBackStack(mainFragment.toString()).commit();
        currentFragment = mainFragment;
        Intent start = new Intent();
        start.setAction(MainActivity.APP_START);
        sendBroadcast(start);

        setAudioDeviceFromPrefs();

        Handler messageHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (MultiCastMessage.class.isInstance(msg.obj)) {
                    MultiCastMessage message = (MultiCastMessage) msg.obj;
                    switch (message.getType()) {
                        case MultiCastMessage.PLAY:
                            onPlayMessageReceived((PlayMessage) message);
                            break;
                        case MultiCastMessage.PREPARE:
                            onPrepareMessageReceived((PrepareMessage) message);
                            break;
                    }
                } else if (msg.obj.getClass() == Boolean.class) {
                    master = (boolean) msg.obj;
                    Log.d(TAG, "AM MASTER IS " + msg.obj);
                    if (master) {
                        onPlaybackStop();
                    }
                }

            }
        };

//        udpMessenger = new UDPMessenger(getBaseContext(), messageHandler, "Lala", 47654) ;
//        udpMessenger.startMessageReceiver();
        jGroupsMessenger = new JGroupsMessenger(messageHandler);
    }

    private void startNextFile() {
        Log.d(TAG, "startNextFile()");
        CampaignFile campaignFile = getNextFile(null);
        Fragment fragment;
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        CampaignFileType fileType = campaignFile != null ? campaignFile.getType() : null;

        fragment = getFragmentByFileType(fileType);

        if (fragment.equals(currentFragment)) {
            Log.w(TAG, "Current fragment stays (onPause, onResume)");
            if (fragment.isResumed()) {
                fragment.onPause();
                fragment.onResume();
            }
        } else {
            try {
                transaction.replace(R.id.main_view, fragment);
                transaction.addToBackStack(fragment.toString());
                transaction.commit();
            } catch (IllegalStateException e) {
                Log.e(TAG, e.getMessage());
                addError(new ErrorMessage(e), false);
                fragment = currentFragment;
            }
        }
        currentFragment = fragment;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, " onActivityResult() ,requestCode = " + requestCode);
        if (requestCode == RESULT_FINISH_FIRST_START) {
            try {
                wrongUuid = false;
                if (mainService != null && data != null) {
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
                addError(new ErrorMessage(ex.toString(), ex.getMessage(), ex.getStackTrace()), false);
            }
        } else if (requestCode == RESULT_FINISH_NETWORK_SETTING) {
            wrongUuid = false;
            try {
                mainService.checkAndDownloadCampaign();
            } catch (RemoteException e) {
//                Nothing here, service updates itself
                Log.w(TAG, e.getMessage());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        hideSystemUI();

        if (mainService != null) {
            try {
                mainService.setClosedNormally(false);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        if (!mBound) {
            Intent intent = new Intent(this, MainService.class);
            bindService(intent, mConnection,
                    Context.BIND_AUTO_CREATE);

            mBound = true;
        }

        if (mainService != null) {
            if (getOrientation() == ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }

            String audioDevice = getAudioDevice();
            if (audioDevice != null) {
                setAudioDevice(audioDevice);
            }
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        if (wrongUuid) {
            startActivityForResult(new Intent(MainActivity.this, FirstActivity.class), RESULT_FINISH_FIRST_START);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");

        if (mainService != null) {
            try {
                mainService.setClosedNormally(true);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

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
        String audioDevice = sharedPref.getString(AUDIO_DEVICE_PREF, "AUDIO_CODEC");
        setAudioDevice(audioDevice);
    }

    private void setAudioDevice(String audioDevice) {
        AudioManager am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        //am.setParameters("audio_devices_out=AUDIO_CODEC,AUDIO_HDMI,AUDIO_SPDIF"); // for reference.
        if (!audioDevice.equals(am.getParameters(AUDIO_DEVICE_PARAM))) {
            am.setParameters(AUDIO_DEVICE_PARAM + "=" + audioDevice);
        }
    }


    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {

            Log.d(TAG, "onServiceConnected");

            mainService = AIDLInterface.Stub.asInterface(binder);

            if (exceptionHandlerError != null) {
                addError(new ErrorMessage("UncaughtException", exceptionHandlerError, null), true);
            }

            boolean isWall = false;
            try {
                isWall = mainService.isVideoWall();
                Log.d(TAG, "I was here mainService.isVideoWall();");
            } catch (RemoteException e) {
//                Will make toast
                makeToast("Could not set VIDEO WALL");
                Log.e(TAG, "Could not set VIDEO WALL RemoteException");
            }
            if (isWall) {
                imageFragment = new FragmentWallImage();
                videoFragment = new FragmentWallVideo();
                videoWall = true;
                jGroupsMessenger.start(getBaseContext());
                Log.d(TAG, "Starting jGroups");
            }

            Campaign serviceCurrent;
            try {
                serviceCurrent = mainService.getCurrentCampaign();
            } catch (RemoteException e) {
                makeToast("Could not get new campaign ");
                Log.e(TAG, "Could not get new campaign " + e.getMessage());
                serviceCurrent = campaign;
            }
            if (campaign == null || !campaign.equals(serviceCurrent)) {
                campaign = serviceCurrent;
                campaignWasUpdated(TAG + " mConnection");
            }

            String uuid;
            try {
                uuid = mainService.getUuid();
            } catch (RemoteException e) {
//                There will be sent a WRONG_UUID message broadcasted after
                Log.w(TAG, "Could not get UUID RemoteException");
                uuid = "";
            }
            if (uuid == null || uuid.equals("fail")) {
                if (!wrongUuid) {
                    wrongUuid = true;
                    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (!InternetConnectionUtil.isNetworkConnected(cm)) {
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

    public void addError(ErrorMessage message, boolean broadcastNow) {
        try {
            mainService.addError(message, broadcastNow);
        } catch (RemoteException e) {
//            Message will be lost
            Log.e(TAG, "Could not add error RemoteException");
        }
    }

    public void makeToast(String toast) {
        boolean silentMode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("silent_mode", false);
        if (!silentMode) {
            Toast.makeText(this, toast, Toast.LENGTH_LONG).show();
        }
    }

    public int getOrientation() {
        if (mainService == null) {
            return ORIENTATION_LANDSCAPE;
        }
        try {
            return mainService.getOrientation();
        } catch (RemoteException e) {
            makeToast("Could not get orientation ");
            Log.e(TAG, "Could not get orientation " + e.getMessage());
            return ORIENTATION_LANDSCAPE;
        }
    }

    private String getAudioDevice() {
        if (mainService == null) {
            return null;
        }
        try {
            return mainService.getAudioDevice();
        } catch (RemoteException e) {
            makeToast("Could not get audio device ");
            Log.e(TAG, "Could not get audio device " + e.getMessage());
            return null;
        }
    }

    private Fragment getFragmentByFileType(CampaignFileType fileType) {
        if (fileType == null) {
            return mainFragment;
        }
        switch (fileType) {
            case IMAGE:
                return imageFragment;
            case AUDIO:
                return audioFragment;
            case VIDEO:
                return videoFragment;
            case HTML:
                return webFragment;
            default:
                return mainFragment;
        }
    }

    private void campaignWasUpdated(String tag) {
        Log.d(tag, "CAMPAIGN_UPDATE to " + (campaign != null ? campaign.getCampaignName() : "NONE"));
        StatusEnum statusEnum = campaign != null ? StatusEnum.NO_ACTIVE_CAMPAIGN : null;
        mainFragment.updateStatus(statusEnum, campaign != null ? campaign.getCampaignName() : NO_ACTIVE_CAMPAIGN);
        startNextFile();
    }

    @Override
    public void onPlaybackStop() {
        Log.w(TAG, "onPlaybackStop");
        startNextFile();
    }

    @Override
    public void onPlayBackRunnableError() {
        String error = null;
        try {
            error = "File with id " + mainService.getCurrentFileId()
                    + " in campaign with id " + campaign.getCampaignId()
                    + " is not playing normally, have to check it";
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        addError(new ErrorMessage("FileError", error, null), false);
    }

    @Override
    public boolean onLongClick(View view) {
        Intent i = new Intent(MainActivity.this, SettingsActivity.class);
        ArrayList<Display> displays = null;
        try {
            if (mainService.getDisplays() != null){
                displays = new ArrayList<>(mainService.getDisplays());
            }
        } catch (RemoteException e) {
            makeToast("Could not get displays ");
            Log.e(TAG, "Could not get displays " + e.getMessage());
        }
        if (displays != null) {
            i.putParcelableArrayListExtra("displays", displays);
        } else {
            Log.w(TAG, "mainService.getDisplays() == nul");
        }

        startActivityForResult(i, RESULT_FINISH_PLAY);
        startActivity(i);
        return true;
    }

    public CampaignFile getNextFile(CampaignFileType fileTypeNeeded) {
        CampaignFile campaignFile = null;
        boolean playingSpecificFile = false;

        if (campaign != null && nextSpecificFile == null &&
                campaign.getFiles() != null && campaign.getFiles().size() > 0) {

            campaignFile = campaign.getNextFile();
//            Log.d(TAG, "getNextFile() filename = " + campaignFile.getName());

            if (campaign.getFiles().size() == 1 && campaignFile != null) {
                campaignFile.setDelay(60 * 60 * 12);
            }


        } else if (nextSpecificFile != null) {
            campaignFile = nextSpecificFile;
            playingSpecificFile = true;
            if (fileTypeNeeded != null) {
                nextSpecificFile = null;
            }

        } else {
            if (campaign != null) {
                mainFragment.updateStatus(StatusEnum.NO_FILES, "No files to play in " + campaign.getCampaignName());
                try {
                    mainService.setCurrentFileId(0);
                } catch (RemoteException e) {
                    Log.w(TAG, "Couldnt send current file id");
                }
            } else {
                mainFragment.updateStatus(StatusEnum.NO_ACTIVE_CAMPAIGN, NO_ACTIVE_CAMPAIGN);
            }
            Log.i(TAG, "CAMPAIGN = NULL");
        }
        CampaignFileType fileType = campaignFile != null ? campaignFile.getType() : null;
        boolean fileTypeOK = fileTypeNeeded != null && fileTypeNeeded.equals(fileType);
        if (fileTypeOK && !playingSpecificFile) {
            setCurrentFileId(campaignFile.getId());
            campaign.setNextFilePosition();
        } else if (fileTypeNeeded != null && !fileTypeNeeded.equals(fileType)) {
            Log.d(TAG, " file type not as needed");
            campaignFile = null;
        }
//
//        campaignFile = new CampaignFile();
//
//        campaignFile.setId(100);
//        campaignFile.setType(CampaignFileType.HTML);
//        campaignFile.setOrderId(1);
//        campaignFile.setPath("http://www.google.ru/");
//        campaignFile.setSize(1);
//        campaignFile.setUpdatedDt(0);
//        campaignFile.setName("not named file");
//        campaignFile.setDelay(60);

        return campaignFile;
    }

    public boolean isMaster() {
        return master;
    }


    public Display getDisplay() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int displayId = Integer.parseInt(sharedPref.getString("monitor_id", "-1"));
        Log.d(TAG, "getDisplay() = " + displayId + " string = " + sharedPref.getString("monitor_id", "-1"));
        DisplayArrayList displays = null;
        try {
            displays = mainService != null ? new DisplayArrayList(mainService.getDisplays()) : null;
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage());
        }
        if (displayId != -1 && displays != null) {
            return displays.getDisplayWithId(displayId);
        } else if (displays != null) {
            Log.w(TAG, "Display not chosen, setting first of " + displays.size() + " displays");
            return displays.get(0);
        } else {
            Log.e(TAG, "mainService is " + (mainService == null ? "NULL" : "NOT null"));
            Log.e(TAG, "mainService.getDisplays() is " + ("NULL"));
            return null;
        }
    }

    public int getWallHeight() throws RemoteException {
        return mainService.getWallHeight();
    }

    public int getWallWidth() throws RemoteException {
        return mainService.getWallWidth();
    }

    public String getAddress() {
        return jGroupsMessenger.getAddress4Char();
    }

    public void setPreviousFilePosition() {
        if (campaign != null) campaign.setPreviousFilePosition();
    }

    private void setCurrentFileId(int currentFileId) {
        Log.d(TAG, "Current file id : " + currentFileId);
        try {
            mainService.setCurrentFileId(currentFileId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        private final String RECEIVER_STRING = TAG + "BroadcastReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case CAMPAIGN_UPDATE:
                    if (mainService == null) return;
                    try {
                        Campaign serviceCampaign = mainService.getCurrentCampaign();

                        boolean updated = campaign != null && !campaign.equals(serviceCampaign)
                                || serviceCampaign != null && serviceCampaign.equals(campaign);
                        campaign = serviceCampaign;
                        mainService.setActivityReceivedUpdate(true);
                        if (updated) {
                            campaignWasUpdated(RECEIVER_STRING);
                        }
                    } catch (RemoteException e) {
                        Log.w(TAG, e.getMessage() + " Will receive same message in 30 sec");
                    }

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
                case WALL_UPDATE:
                    Log.d(RECEIVER_STRING, WALL_UPDATE);
                    if (videoWall) {
                        currentFragment.onPause();
                        currentFragment.onResume();
                    } else {
                        jGroupsMessenger.start(getBaseContext());
                        imageFragment = new FragmentWallImage();
                        videoFragment = new FragmentWallVideo();
                        videoWall = true;
                        startNextFile();
                    }
                    break;
                case SETTINGS_UUID_CHANGE:
                    Log.d(RECEIVER_STRING, SETTINGS_UUID_CHANGE);
                    String uuid = intent.getStringExtra("uuid");
                    try {
                        mainService.setUuid(uuid);
                    } catch ( RemoteException e ){
                        makeToast("Could not set UUID, please try again later.");
                    }
            }
        }
    };

    @Override
    public void onPlayMessageReceived(PlayMessage message) {
        Log.d(TAG, "onPlayMessageReceived");
        if (!videoWall) {
            return;
        }
        Campaign campaign;
        try {
            campaign = mainService.getCampaignWithId(message.getCampaignId());
        } catch (RemoteException e) {
            return;
        }
        if (campaign == null) return;
        CampaignFile campaignFile = campaign.getFileById(message.getFileId());
        if (campaignFile == null) return;
        FragmentVideoWall fragmentByType = (FragmentVideoWall) getFragmentByFileType(campaignFile.getType());
        if (!fragmentByType.equals(currentFragment)) {
            startNextFile();
        }
        fragmentByType.playFile(campaignFile, message.getFrameId());

    }

    @Override
    public void onPrepareMessageReceived(PrepareMessage message) {
        Log.d(TAG, "onPrepareMessageReceived");
        if (!videoWall) {
            return;
        }
        Campaign campaign;
        try {
            campaign = mainService.getCampaignWithId(message.getCampaignId());
        } catch (RemoteException e) {
            return;
        }
        CampaignFile campaignFile = campaign != null ? campaign.getFileById(message.getFileId()) : null;
        if (campaignFile == null) return;
        FragmentVideoWall fragment = (FragmentVideoWall) getFragmentByFileType(campaignFile.getType());
        fragment.prepareFile(campaignFile);
    }


    @Override
    public void onFileNotPrepared() {
        Log.d(TAG + "MasterListener", "onFileNotPrepared");
//        udpMessenger.sendMessage(new PrepareMessage("1111",campaign.getCampaignId(),getNextFile(null).getId()));
        jGroupsMessenger.sendMessage(new PrepareMessage(getAddress(), campaign.getCampaignId(), getNextFile(null).getId()));
    }

    @Override
    public void onFileStartedPlaying(int fileId, long frameId) {
        Log.d(TAG + "MasterListener", "onFileStartedPlaying");
//        udpMessenger.sendMessage(new PlayMessage("1111",campaign.getCampaignId(),fileId,frameId));
        jGroupsMessenger.sendMessage(new PlayMessage(getAddress(), campaign.getCampaignId(), fileId, frameId));
    }
}
