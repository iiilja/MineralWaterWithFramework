package ee.promobox.promoboxandroid.interfaces;

import android.view.View;

/**
 * Created by ilja on 18.02.2015.
 */
public interface PlayerButtonsClickListener extends View.OnClickListener {
    public void  onPlayerPause();
    public void  onPlayerPlay();
    public void  onPlayerPrevious();
    public void  onPlayerNext();
    public void  onSettingsPressed();
}
