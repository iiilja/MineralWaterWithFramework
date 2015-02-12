package ee.promobox.promoboxandroid.util;

import android.util.Log;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Created by ilja on 11.02.2015.
 */
public class PlayerUIVisibilityRunnable implements Runnable {
    private final String TAG = "PlayerUIVisibilityRunnable";
    private final WeakReference<View> seekBarLayoutReference;
    private final WeakReference<View> playerButtonsLayoutReference;

    public PlayerUIVisibilityRunnable(View seekBarLayout, View playerButtonsLayout){
        seekBarLayoutReference = new WeakReference<>(seekBarLayout);
        playerButtonsLayoutReference = new WeakReference<>(playerButtonsLayout);
    }

    @Override
    public void run() {
        Log.d(TAG, "PlayerUIVisibilityRunnable");
        View seekBarLayout = seekBarLayoutReference.get();
        View playerButtonsLayout = playerButtonsLayoutReference.get();
        if (seekBarLayout != null && playerButtonsLayout != null) {
            seekBarLayout.setVisibility(View.INVISIBLE);
            playerButtonsLayout.setVisibility(View.INVISIBLE);
        }
    }
}

