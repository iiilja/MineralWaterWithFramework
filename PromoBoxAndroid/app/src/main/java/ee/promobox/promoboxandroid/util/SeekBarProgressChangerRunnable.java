package ee.promobox.promoboxandroid.util;

import android.widget.SeekBar;

import java.lang.ref.WeakReference;

/**
 * Created by ilja on 11.02.2015.
 */
public class SeekBarProgressChangerRunnable implements Runnable {
    private String TAG = "SeekBarProgressChanger";

    private boolean isKilled = false;

    private final WeakReference<SeekBar> seekBarWeakReference;

    public SeekBarProgressChangerRunnable(SeekBar seekBar){
        seekBarWeakReference = new WeakReference<>(seekBar);
    }

    @Override
    public void run() {
        SeekBar seekBar = seekBarWeakReference.get();
        if (!isKilled && seekBar != null && seekBar.getProgress() < seekBar.getMax()) {
            seekBar.incrementProgressBy(100);
//            Log.d(TAG, "progress = " + seekBar.getProgress());
            seekBar.postDelayed(this, 100);
        }
    }
}
