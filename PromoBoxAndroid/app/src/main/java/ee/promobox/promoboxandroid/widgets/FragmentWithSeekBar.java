package ee.promobox.promoboxandroid.widgets;

import android.app.Fragment;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import ee.promobox.promoboxandroid.R;
import ee.promobox.promoboxandroid.SettingsActivity;
import ee.promobox.promoboxandroid.interfaces.PlayerButtonsClickListener;
import ee.promobox.promoboxandroid.util.PlayerUIVisibilityRunnable;
import ee.promobox.promoboxandroid.util.SeekBarProgressChangerRunnable;

/**
 * Created by ilja on 12.02.2015.
 */
public abstract class FragmentWithSeekBar extends Fragment implements PlayerButtonsClickListener ,
        SeekBar.OnSeekBarChangeListener{
    private static final String TAG = "FragmentWithSeekBar";

    private static final long VISIBILITY_DELAY_MS = 20*1000;

    private Handler playerUIVisibilityHandler = new Handler();
    private Runnable visibilityRunnable;
    private SeekBarProgressChangerRunnable seekBarProgressChanger;

    public boolean paused = false;

    private SeekBar seekBar;
    private View playerControlsLayout;

    protected void setView(View view) {
        seekBar = (SeekBar) view.findViewById(R.id.audio_seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setProgressDrawable( getResources().getDrawable(R.drawable.seek_bar_progress));
        seekBar.setThumb(getResources().getDrawable(R.drawable.seek_bar_thumb_scrubber_control_selector_holo_dark));
        playerControlsLayout = view.findViewById(R.id.player_controls);
        seekBarProgressChanger = new SeekBarProgressChangerRunnable(seekBar);
        visibilityRunnable = new PlayerUIVisibilityRunnable(playerControlsLayout);
        playerUIVisibilityHandler.postDelayed(visibilityRunnable, VISIBILITY_DELAY_MS);

        Button pauseButton = (Button) playerControlsLayout.findViewById(R.id.player_pause);
        Button previousButton = (Button) playerControlsLayout.findViewById(R.id.player_back);
        Button nextButton = (Button) playerControlsLayout.findViewById(R.id.player_next);
        Button settingsButton = (Button) playerControlsLayout.findViewById(R.id.player_settings);
        pauseButton.setOnClickListener(this);
        previousButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);

        view.setOnClickListener(this);
    }

    public void cleanUp(){
        if (seekBar != null ){
            seekBar.removeCallbacks(seekBarProgressChanger);
            seekBar.setProgress(0);
            seekBar.setMax(100);
        }

    }

    protected void setSeekBarMax( long duration ){
        seekBar.setMax((int) duration);
        TextView playerFullTime = (TextView) playerControlsLayout.findViewById(R.id.player_time_full);
        playerFullTime.setText(getTimeString(duration));
    }

    protected void changeSeekBarState (boolean startingPlaying, int progress){
        seekBar.setProgress(progress);
        handleSeekBarRunnable(!startingPlaying);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        TextView timeElapsed = (TextView) playerControlsLayout.findViewById(R.id.player_time_elapsed);
        timeElapsed.setText(getTimeString(progress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onClick(View v) {

        playerUIVisibilityHandler.removeCallbacks(visibilityRunnable);
        if (playerControlsLayout.getVisibility() == View.VISIBLE) {
            playerUIVisibilityHandler.postDelayed(visibilityRunnable, VISIBILITY_DELAY_MS);
        }

        switch (v.getId()){
            case R.id.player_settings:
                Intent i = new Intent(getActivity(), SettingsActivity.class);
                startActivity(i);
                return;
            case R.id.player_back:
                onPlayerPrevious();
                return;
            case R.id.player_pause:
                paused = !paused;
                handleSeekBarRunnable(paused);
                if (paused) {
                    onPlayerPause();
                } else {
                    onPlayerPlay();
                }
                return;
            case R.id.player_next:
                onPlayerNext();
                return;
        }

        int newVisibility = playerControlsLayout.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE;
        playerControlsLayout.setVisibility(newVisibility);
    }


    private void handleSeekBarRunnable(boolean isPausedNow){
        seekBar.removeCallbacks(seekBarProgressChanger);
        Button pauseButton = (Button) playerControlsLayout.findViewById(R.id.player_pause);
        if (isPausedNow) {
            Log.d(TAG, "PAUSED NOW");
            pauseButton.setBackground(getResources().getDrawable(R.drawable.player_play));
        } else {
            Log.d(TAG, "PLAYING NOW");
            seekBar.post(seekBarProgressChanger);
            pauseButton.setBackground(getResources().getDrawable(R.drawable.player_pause));
        }
    }


    private String getTimeString ( long timeMillis) {
        String hms;
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

    public int getRemainingTime(){
        return seekBar.getMax() - seekBar.getProgress();
    }
}
