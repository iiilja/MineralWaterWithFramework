package ee.promobox.promoboxandroid.interfaces;

/**
 * Created by ilja on 27.02.2015.
 */
public interface VideoWallMasterListener {
    public void onFileNotPrepared();
    public void onFileStartedPlaying(int fileId, long frameId);

}