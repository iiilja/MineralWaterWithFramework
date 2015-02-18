package ee.promobox.promoboxandroid.util;

import android.util.Log;
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
        Log.w(TAG, (isKilled ? "AM KILLED": "am alive") + " my name is " + toString());
        if (!isKilled && seekBar != null && seekBar.getProgress() < seekBar.getMax()) {
            seekBar.incrementProgressBy(1000);
            Log.d(TAG, "progress = " + seekBar.getProgress());
            seekBar.postDelayed(this, 1000);
        }
    }
//
//    public void setKilled(boolean isKilled) {
//        this.isKilled = isKilled;
//    }
}
