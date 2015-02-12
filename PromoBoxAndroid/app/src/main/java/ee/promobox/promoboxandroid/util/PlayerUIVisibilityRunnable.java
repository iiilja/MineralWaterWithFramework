package ee.promobox.promoboxandroid.util;

import android.util.Log;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Created by ilja on 11.02.2015.
 */
public class PlayerUIVisibilityRunnable implements Runnable {
    private final String TAG = "PlayerUIVisibilityRunnable";
    private final WeakReference<View> playerControlsLayoutLayoutReference;

    public PlayerUIVisibilityRunnable(View playerControlsLayout){
        playerControlsLayoutLayoutReference = new WeakReference<>(playerControlsLayout);
    }

    @Override
    public void run() {
        Log.d(TAG, "PlayerUIVisibilityRunnable");
        View playerControlsLayout = playerControlsLayoutLayoutReference.get();
        if (playerControlsLayout != null) {
            playerControlsLayout.setVisibility(View.INVISIBLE);
        }
    }
}

