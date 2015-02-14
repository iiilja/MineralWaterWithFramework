package ee.promobox.promoboxandroid.util;

import android.util.Log;
import android.widget.SeekBar;

import java.lang.ref.WeakReference;

/**
 * Created by ilja on 11.02.2015.
 */
public class SeekBarProgressChangerRunnable implements Runnable {
    private String TAG = "SeekBarProgressChanger";

    private final WeakReference<SeekBar> seekBarWeakReference;

    public SeekBarProgressChangerRunnable(SeekBar seekBar){
        seekBarWeakReference = new WeakReference<>(seekBar);
    }

    @Override
    public void run() {
        SeekBar seekBar = seekBarWeakReference.get();
        if (seekBar != null && seekBar.getProgress() < seekBar.getMax()) {
            seekBar.incrementProgressBy(1000);
            Log.d(TAG, "progress = " + seekBar.getProgress());
            seekBar.postDelayed(this, 1000);
        }
    }
}
