package ee.promobox.promoboxandroid.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import java.lang.ref.WeakReference;

import ee.promobox.promoboxandroid.widgets.FragmentWithSeekBar;

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
        Log.d(TAG, TAG);
        View playerControlsLayout = playerControlsLayoutLayoutReference.get();
        if (playerControlsLayout != null) {
            playerControlsLayout.setVisibility(View.INVISIBLE);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(playerControlsLayout.getContext());
            preferences.edit().putInt(FragmentWithSeekBar.PLAYER_UI_VISIBILITY,View.INVISIBLE).apply();
        }
    }
}

