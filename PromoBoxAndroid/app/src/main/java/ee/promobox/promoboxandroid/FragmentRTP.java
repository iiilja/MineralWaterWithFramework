package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ee.promobox.promoboxandroid.data.CampaignFile;
import ee.promobox.promoboxandroid.data.CampaignFileType;
import ee.promobox.promoboxandroid.interfaces.FragmentPlaybackListener;
import ee.promobox.promoboxandroid.interfaces.PlayerButtonsClickListener;
import ee.promobox.promoboxandroid.util.vitamio.MediaController;
import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.VideoView;


public class FragmentRTP extends Fragment implements MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener, PlayerButtonsClickListener {

    private static final String TAG = "FragmentRTP ";

    private View fragmentVideoLayout;
    private VideoView videoView;

    private FragmentPlaybackListener playbackListener;
    private MainActivity mainActivity;

    private final int bufferingCheckDelay = 5000;
    private Handler bufferingCheckerHandler = new Handler();
    private Runnable bufferingChecker = new BufferingCheckerRunnable();




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView");
        boolean initialised = Vitamio.initialize(getActivity(), R.raw.libarm);
//        Log.d(TAG, initialised ? "initialised " : "not initialised");
        if (!LibsChecker.checkVitamioLibs(getActivity())){
            Log.e(TAG, "No vitamio libs");
            return null;
        }
        fragmentVideoLayout =  inflater.inflate(R.layout.fragment_rtp,container,false);
        fragmentVideoLayout.setOnLongClickListener(mainActivity);
        videoView = (VideoView) fragmentVideoLayout.findViewById(R.id.surface_view);

        videoView.setMediaController(new MediaController(getActivity(), this));
        videoView.setMediaBufferingIndicator(fragmentVideoLayout.findViewById(R.id.main_activity_status));
        videoView.setOnErrorListener(this);
        videoView.setOnCompletionListener(this);
//        videoView.setOnInfoListener(this);
//        videoView.setOnBufferingUpdateListener(this);
        fragmentVideoLayout.setOnLongClickListener(mainActivity);
//        super.setView(fragmentVideoLayout);
        return fragmentVideoLayout;
    }

    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach");
        super.onAttach(activity);
        playbackListener = (FragmentPlaybackListener) activity;
        mainActivity = (MainActivity) activity;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mainActivity.getOrientation() == MainActivity.ORIENTATION_PORTRAIT_EMULATION){
//            videoView.setRotation(270.0f);
            fragmentVideoLayout.setRotation(270);
        }
        tryNextFile();

    }

    @Override
    public void onPause() {
        if (videoView != null ){
            videoView.stopPlayback();
        }
        super.onPause();
    }

    private void tryNextFile() {
        CampaignFile campaignFile = mainActivity.getNextFile(CampaignFileType.RTP);

        bufferingCheckerHandler.removeCallbacks(bufferingChecker);

        if (campaignFile != null) {
            playVideo(campaignFile);
        } else {
            cleanUp();
            playbackListener.onPlaybackStop();
        }
    }

    public void cleanUp() {
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }

    public void playVideo(CampaignFile campaignFile) {
        Log.d(TAG, "Playing LINK " + campaignFile.getName());
        if (videoView == null ) {
            videoView = (VideoView) fragmentVideoLayout.findViewById(R.id.surface_view);
            if (videoView == null ) {
                Log.e(TAG,"Not found");
                return;
            }
        }
        videoView.setVideoURI(Uri.parse(campaignFile.getName()));
        videoView.requestFocus();
        bufferingCheckerHandler.postDelayed(bufferingChecker, bufferingCheckDelay);

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                // optional need Vitamio 4.0
                Log.d(TAG, "Prepared");
                mediaPlayer.setPlaybackSpeed(1.0f);
                mediaPlayer.start();
            }
        });
    }

    private void makeToast(String toast){
        mainActivity.makeToast(toast);
    }


    @Override
    public void onPlayerPause() {
        Log.d(TAG, "onPlayerPause");
        videoView.pause();
    }

    @Override
    public void onPlayerPlay() {
        Log.d(TAG, "onPlayerPlay");
        videoView.start();
    }

    @Override
    public void onPlayerPrevious() {
        mainActivity.setPreviousFilePosition();
        tryNextFile();
    }

    @Override
    public void onPlayerNext() {
        cleanUp();
        tryNextFile();
    }

    @Override
    public void onSettingsPressed() {
        fragmentVideoLayout.performLongClick();
    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "MediaPlayerError ");
        cleanUp();
        tryNextFile();
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "MediaPlayer onCompletion ");
        cleanUp();
        tryNextFile();
    }



    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        Log.d(TAG, "onBufferingUpdate " + percent);
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "click");

        switch (v.getId()){
            case R.id.player_settings:
                fragmentVideoLayout.performLongClick();
                break;
            case R.id.player_back:
                onPlayerPrevious();
                break;
            case R.id.player_next:
                onPlayerNext();
                break;
        }
    }

    private class BufferingCheckerRunnable implements Runnable {
        private int previousPercent = -1;
        @Override
        public void run() {
            if (videoView == null){
                Log.e(TAG, "videoView is null");
            } else if (!videoView.isBuffering()){
                Log.d(TAG, "Is not buffering");
            } else if ( videoView.getBufferPercentage() != previousPercent){
                Log.d(TAG, "Procents not equal : prev = " + previousPercent + " current = " + videoView.getBufferPercentage());
                previousPercent = videoView.getBufferPercentage();
            } else {
                Log.e(TAG, "Too long buffering");
                videoView.stopPlayback();
                tryNextFile();
                return;
            }
            bufferingCheckerHandler.postDelayed(this, bufferingCheckDelay);
        }
    }
}
