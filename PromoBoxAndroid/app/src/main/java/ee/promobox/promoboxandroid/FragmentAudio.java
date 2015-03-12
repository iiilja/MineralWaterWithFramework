package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.FrameworkSampleSource;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.SampleSource;

import java.io.File;
import java.io.FileNotFoundException;

import ee.promobox.promoboxandroid.data.CampaignFile;
import ee.promobox.promoboxandroid.data.CampaignFileType;
import ee.promobox.promoboxandroid.data.ErrorMessage;
import ee.promobox.promoboxandroid.interfaces.FragmentPlaybackListener;
import ee.promobox.promoboxandroid.util.PlayerLengthWatcher;
import ee.promobox.promoboxandroid.widgets.FragmentWithSeekBar;
import ee.promobox.promoboxandroid.widgets.MyAnimatedDrawable;


//https://github.com/felixpalmer/android-visualizer
public class FragmentAudio extends FragmentWithSeekBar implements ExoPlayer.Listener{

    private static final String TAG = "AudioActivity ";

    private ExoPlayer exoPlayer;
    private MediaCodecAudioTrackRenderer audioRenderer;

    private FragmentPlaybackListener playbackListener;
    private MainActivity mainActivity;

    private Handler audioLengthHandler = new Handler();
    private static Runnable audioLengthStopper;

    private View audioView;
    private MyAnimatedDrawable audioAnimation;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        audioView = inflater.inflate(R.layout.fragment_audio, container, false);
        audioAnimation = new MyAnimatedDrawable(mainActivity.getBaseContext(), MyAnimatedDrawable.AUDIO, 0, 23);
        audioView.setBackground(audioAnimation);
        audioAnimation.start();
        super.setView(audioView);
        audioView.setOnLongClickListener(mainActivity);


        return audioView;
    }

    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach");
        playbackListener = (FragmentPlaybackListener) activity;

        audioLengthStopper = new PlayerLengthWatcher(this,playbackListener);

        mainActivity = (MainActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();


        if ( mainActivity.getOrientation() == MainActivity.ORIENTATION_PORTRAIT_EMULATION){
            audioView.setRotation(270);
        }
        tryNextFile();

    }


    @Override
    public void onDestroy() {
        cleanUp();
        audioAnimation.recycleSelf();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        cleanUp();
    }


    private void playAudio(CampaignFile campaignFile) throws Exception{
        cleanUp();
        String pathToFile = campaignFile.getPath();
        File file = new File(pathToFile);
        if (!file.exists()){
            Log.e(TAG, "File not found : " + pathToFile);
            throw new FileNotFoundException("File not found : " + pathToFile);
        }
        Log.d(TAG,"playAudio() file = " + file.getName() + " PATH = " + pathToFile);
        setStatus(campaignFile.getName());
        Uri uri = Uri.parse(pathToFile);
        SampleSource source = new FrameworkSampleSource(getActivity(), uri, null, 1);
        audioRenderer = new MediaCodecAudioTrackRenderer(source);
        exoPlayer = ExoPlayer.Factory.newInstance(1);
        exoPlayer.prepare(audioRenderer);
        exoPlayer.setPlayWhenReady(true);
        exoPlayer.addListener(this);
    }

    public void cleanUp() {
        super.cleanUp();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        audioRenderer = null;

        audioLengthHandler.removeCallbacks(audioLengthStopper);

    }


    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_PREPARING){
            Log.d(TAG, "ExoPlayer.STATE_PREPARING ");
        }
        if (playbackState == ExoPlayer.STATE_READY){
            long duration = exoPlayer.getDuration();
            long position = exoPlayer.getCurrentPosition();
            Log.d(TAG, "ExoPlayer.STATE_READY , exoPlayer.getDuration()" + duration);
            if (duration != ExoPlayer.UNKNOWN_TIME){
                super.setSeekBarMax( duration );
                if (playWhenReady) {
                    audioAnimation.start();
                    super.changeSeekBarState(playWhenReady,(int) exoPlayer.getCurrentPosition());
                    audioLengthHandler.postDelayed(audioLengthStopper, duration -position + 10 * 1000);
                }
            }
        }
        if (playbackState == ExoPlayer.STATE_ENDED) {
            tryNextFile();
        }
    }

    @Override
    public void onPlayWhenReadyCommitted() {
//        Log.d(TAG, "onPlayWhenReadyCommitted");
    }

    @Override
    public void onPlayerError(ExoPlaybackException ex) {
        mainActivity.makeToast("Audio player error");
        mainActivity.addError(new ErrorMessage(ex), false);
        Log.e(TAG, "onPlayerError " + ex.getMessage());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                tryNextFile();
            }
        }, 1000);
    }

    private void finishActivity (){
        cleanUp();
        playbackListener.onPlaybackStop();
    }

    private void tryNextFile(){
        try {
            CampaignFile campaignFile = mainActivity.getNextFile(CampaignFileType.AUDIO);
            if (campaignFile != null){
                playAudio(campaignFile);
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finishActivity();
                    }
                }, 1000);
            }
        }
        catch (Exception ex){
            Log.e(TAG, "onPlayerError " + ex.getMessage());
            mainActivity.makeToast(String.format(
                    MainActivity.ERROR_MESSAGE, 12, ex.getClass().getSimpleName()));
            mainActivity.addError(new ErrorMessage(ex), false);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    tryNextFile();
                }
            }, 1000);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        long progress = seekBar.getProgress();
        if (exoPlayer != null){
            exoPlayer.seekTo(progress);

            audioLengthHandler.removeCallbacks(audioLengthStopper);
            audioLengthHandler.postDelayed(audioLengthStopper, seekBar.getMax()-progress + 3 * 1000);
        }
    }

    @Override
    public void onPlayerPause() {
        Log.d(TAG, "onPlayerPause");
        audioAnimation.stop();
        audioLengthHandler.removeCallbacks(audioLengthStopper);
        exoPlayer.setPlayWhenReady(false);
    }

    @Override
    public void onPlayerPlay() {
        Log.d(TAG, "onPlayerPlay");

        audioLengthHandler.postDelayed(audioLengthStopper, exoPlayer.getDuration()-exoPlayer.getCurrentPosition() + 3 * 1000);
        exoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void onPlayerPrevious() {
        mainActivity.setPreviousFilePosition();
        tryNextFile();
    }

    @Override
    public void onPlayerNext() {
        tryNextFile();
    }


}
