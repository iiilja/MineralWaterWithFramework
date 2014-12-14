package ee.promobox.promoboxandroid.util;

import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

/**
 * Created by Viktor on 11/13/2014.
 * Class constantly tracks media player position.
 */
public class SoundFadeAnimation implements Runnable {

    private static final int ANIMATION_SMOOTHNESS = 1000; // how often animation updates in ms.
    private static final int DEFAULT_START_DURATION = 3000;
    private static final int DEFAULT_END_DURATION = 3000;

    private MediaPlayer mediaPlayer;
    private FadeCallback fadeCallback;
    private Handler handler;
    private int startDuration;
    private int endDuration;
    private boolean fadeStart;

    public SoundFadeAnimation(MediaPlayer mediaPlayer) {
        this(mediaPlayer, DEFAULT_START_DURATION, DEFAULT_END_DURATION);
    }

    public SoundFadeAnimation(MediaPlayer mediaPlayer, int startDuration, int endDuration) {
        this.mediaPlayer = mediaPlayer;
        this.handler = new Handler();
        this.startDuration = startDuration;
        this.endDuration = endDuration;
        this.run();
    }

    public void cleanUp() {
        mediaPlayer = null;
        if(handler != null) {
            handler.removeCallbacks(this);
            handler = null;
        }
    }

    @Override
    public void run() {
        if(mediaPlayer != null) {
            if(mediaPlayer.isPlaying()) {
                float volume;
                int wait;

                int currentPosition = mediaPlayer.getCurrentPosition();
                if (startDuration != 0 && currentPosition < startDuration) {
                    volume = currentPosition / (float)startDuration;
                    wait = ANIMATION_SMOOTHNESS;
                    runAfterWait(wait);
                } else if(endDuration != 0) {
                    int songDuration = mediaPlayer.getDuration();
                    // time end animation should be starting.
                    int endAnimationStart = songDuration - endDuration;
                    if(currentPosition > endAnimationStart) {
                        if(fadeStart) {
                            fadeStart = false;
                            if(fadeCallback != null) {
                                fadeCallback.onFade();
                            }
                        }
                        volume = (songDuration - currentPosition) / (float)endDuration;
                        wait = ANIMATION_SMOOTHNESS;
                    } else {
                        fadeStart = true;
                        volume = 1f;
                        // wait time until we can start fade out animation
                        wait = endAnimationStart - currentPosition;
                    }
                    runAfterWait(wait);
                } else {
                    volume = 1f;
                }
                mediaPlayer.setVolume(volume, volume);
            } else {
                // Wait 1 second for player to start  playing.
                runAfterWait(1000);
            }
        } else {
            Log.w("SoundFadeAnimation", "Animation Failed. reason: media player is null. May fire on unexpected cleanup.");
        }
    }

    private void runAfterWait(int waitTime) {
        if (handler != null) {
            handler.postDelayed(this, waitTime);
        } else {
            Log.w("SoundFadeAnimation", "Animation Failed. reason: handler is null. May fire on unexpected cleanup.");
        }
    }

    public void setOnFadeCallback(FadeCallback fadeCallback) {
        this.fadeCallback = fadeCallback;
    }

    public interface FadeCallback { public void onFade(); }
}