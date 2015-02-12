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
        Log.d(TAG, "SeekBarProgressChanger");
        SeekBar seekBar = seekBarWeakReference.get();
        while (seekBar != null && seekBar.getProgress() < seekBar.getMax()) {
            seekBar.incrementProgressBy(250);
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
