package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.app.Fragment;
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
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import ee.promobox.promoboxandroid.data.CampaignFile;
import ee.promobox.promoboxandroid.data.CampaignFileType;
import ee.promobox.promoboxandroid.data.ErrorMessage;
import ee.promobox.promoboxandroid.util.FragmentPlaybackListener;
import ee.promobox.promoboxandroid.util.PlayerUIVisibilityRunnable;
import ee.promobox.promoboxandroid.util.SeekBarProgressChangerRunnable;


//https://github.com/felixpalmer/android-visualizer
public class FragmentAudio extends Fragment implements ExoPlayer.Listener, SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    private static final String TAG = "AudioActivity ";
    private static final long VISIBILITY_DELAY_MS = 10*1000;

    private ExoPlayer exoPlayer;
    private MediaCodecAudioTrackRenderer audioRenderer;

    private FragmentPlaybackListener playbackListener;
    private MainActivity mainActivity;

    private Handler audioLengthHandler = new Handler();
    private Handler playerUIVisibilityHandler = new Handler();
    private static Runnable visibilityRunnable;
    private static Runnable seekBarProgressChanger;
    private static Runnable audioLengthStopper;

    private View audioView;
    private SeekBar seekBar;
    private View seekBarLayout;

    private View playerButtonsLayout;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audio, container, false);

        view.setOnLongClickListener(mainActivity);
        view.setOnClickListener(this);
        audioView = view;
        seekBar = (SeekBar) view.findViewById(R.id.audio_seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setProgressDrawable( getResources().getDrawable(R.drawable.seek_bar_progress));
        seekBar.setThumb(getResources().getDrawable(R.drawable.seek_bar_thumb_scrubber_control_selector_holo_dark));
        seekBarLayout = view.findViewById(R.id.seekBar_layout);
        playerButtonsLayout = view.findViewById(R.id.audio_player_buttons);
//        seekBar.setIndeterminate(true);
        seekBarProgressChanger = new SeekBarProgressChangerRunnable(seekBar);
        visibilityRunnable = new PlayerUIVisibilityRunnable(seekBarLayout, playerButtonsLayout);
        playerUIVisibilityHandler.postDelayed(visibilityRunnable, VISIBILITY_DELAY_MS);
        return view;
    }


    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach");
        playbackListener = (FragmentPlaybackListener) activity;

        audioLengthStopper = new AudioLengthWatcher(this,playbackListener);

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

    private void cleanUp() {
        if (seekBar != null ){
            seekBar.setProgress(0);
            seekBar.setMax(100);
        }
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        audioRenderer = null;

        audioLengthHandler.removeCallbacks(audioLengthStopper);

    }


    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_READY){
            long duration = exoPlayer.getDuration();
            Log.d(TAG, "onPlayerStateChanged , exoPlayer.getDuration()" + duration);
            if (duration != ExoPlayer.UNKNOWN_TIME){
                seekBar.setMax((int) duration);
                TextView playerFullTime = (TextView) seekBarLayout.findViewById(R.id.player_time_full);
                playerFullTime.setText(getTimeString(duration));
                audioLengthHandler.postDelayed(audioLengthStopper, duration + 10 * 1000);
            }
        }
        if (playbackState == ExoPlayer.STATE_ENDED) {
            tryNextFile();
        }
    }

    @Override
    public void onPlayWhenReadyCommitted() {
        new Thread(seekBarProgressChanger).start();
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

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        TextView timeElapsed = (TextView) seekBarLayout.findViewById(R.id.player_time_elapsed);
        timeElapsed.setText(getTimeString(progress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        long progress = seekBar.getProgress();
        if (exoPlayer != null){
            exoPlayer.seekTo(progress);

            audioLengthHandler.removeCallbacks(audioLengthStopper);
            audioLengthHandler.postDelayed(audioLengthStopper, seekBar.getMax()-progress + 10 * 1000);
        }
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

    private void setStatus(String status){
        TextView textView = (TextView) audioView.findViewById(R.id.main_activity_status);
        textView.setText(status);
    }

    private String getTimeString ( long timeMillis) {
        String hms = getResources().getString(R.string.player_unknown_time);
        if ( TimeUnit.MILLISECONDS.toHours(timeMillis) > 1 ) {
            hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(timeMillis),
                    TimeUnit.MILLISECONDS.toMinutes(timeMillis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeMillis)),
                    TimeUnit.MILLISECONDS.toSeconds(timeMillis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeMillis)));
        } else {
            hms = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(timeMillis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeMillis)),
                    TimeUnit.MILLISECONDS.toSeconds(timeMillis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeMillis)));
        }
        return hms;
    }

    @Override
    public void onClick(View v) {
        playerUIVisibilityHandler.removeCallbacks(visibilityRunnable);
        int visibility = seekBarLayout.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE;
        seekBarLayout.setVisibility(visibility);
        playerButtonsLayout.setVisibility(visibility);
        if (visibility == View.VISIBLE) {
            playerUIVisibilityHandler.postDelayed(visibilityRunnable, VISIBILITY_DELAY_MS);
        }
    }


    private static final class AudioLengthWatcher implements Runnable {
        private final WeakReference<FragmentAudio> fragmentAudioReference;
        private final WeakReference<FragmentVideo> fragmentVideoReference;
        private final WeakReference<FragmentPlaybackListener> playbackListenerReference;

        AudioLengthWatcher( FragmentAudio fragment, FragmentPlaybackListener playbackListener){
            fragmentAudioReference = new WeakReference<>(fragment);
            fragmentVideoReference =  null;
            playbackListenerReference = new WeakReference<>(playbackListener);
        }
        AudioLengthWatcher( FragmentVideo fragment, FragmentPlaybackListener playbackListener){
            fragmentVideoReference = new WeakReference<>(fragment);
            fragmentAudioReference = null;
            playbackListenerReference = new WeakReference<>(playbackListener);
        }

        @Override
        public void run() {
            Log.e(FragmentAudio.TAG,"Executing runnable, smth wrong with player");
            FragmentAudio fragment = fragmentAudioReference.get();
            FragmentPlaybackListener playbackListener = playbackListenerReference.get();
            if (fragment != null && playbackListener != null){
                fragment.onPause();
                fragment.onResume();
                playbackListener.onPlaybackStop();
            }
        }
    }

}
