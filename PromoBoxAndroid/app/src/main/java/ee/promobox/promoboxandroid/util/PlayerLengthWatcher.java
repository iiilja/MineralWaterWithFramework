package ee.promobox.promoboxandroid.util;

import android.util.Log;

import java.lang.ref.WeakReference;

import ee.promobox.promoboxandroid.widgets.FragmentWithSeekBar;

/**
 * Created by ilja on 26.02.2015.
 */
public class PlayerLengthWatcher  implements Runnable {
    private final WeakReference<FragmentWithSeekBar> fragmentReference;
    private final WeakReference<FragmentPlaybackListener> playbackListenerReference;

    public PlayerLengthWatcher(FragmentWithSeekBar fragment, FragmentPlaybackListener playbackListener){
        fragmentReference = new WeakReference<>(fragment);
        playbackListenerReference = new WeakReference<>(playbackListener);
    }

    @Override
    public void run() {
        Log.e("PlayerLengthWatcher", "Executing runnable, smth wrong with player");
        FragmentWithSeekBar fragment = fragmentReference.get();
        FragmentPlaybackListener playbackListener = playbackListenerReference.get();
        if (fragment != null && playbackListener != null){
            fragment.cleanUp();
            playbackListener.onPlaybackStop();
        }
    }
}
