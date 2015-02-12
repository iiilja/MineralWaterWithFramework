package ee.promobox.promoboxandroid.widgets;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import ee.promobox.promoboxandroid.R;
import ee.promobox.promoboxandroid.util.PlayerUIVisibilityRunnable;
import ee.promobox.promoboxandroid.util.SeekBarProgressChangerRunnable;

/**
 * Created by ilja on 12.02.2015.
 */
public abstract class FragmentWithSeekBar extends Fragment implements View.OnClickListener , SeekBar.OnSeekBarChangeListener{
    private static final long VISIBILITY_DELAY_MS = 60*1000;

    private Handler playerUIVisibilityHandler = new Handler();
    private static Runnable visibilityRunnable;
    private static Runnable seekBarProgressChanger;

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

        view.setOnClickListener(this);
    }

    protected void cleanUp(){
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

    protected void startSeekBarProgressChanger(){
        seekBar.post(seekBarProgressChanger);
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
        int visibility = playerControlsLayout.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE;
        playerControlsLayout.setVisibility(visibility);
        if (visibility == View.VISIBLE) {
            playerUIVisibilityHandler.postDelayed(visibilityRunnable, VISIBILITY_DELAY_MS);
        }
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


}
