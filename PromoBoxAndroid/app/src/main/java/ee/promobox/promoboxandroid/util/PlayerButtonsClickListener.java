package ee.promobox.promoboxandroid.util;

import android.view.View;
import android.widget.Button;

/**
 * Created by ilja on 18.02.2015.
 */
public interface PlayerButtonsClickListener extends View.OnClickListener {
    public void  onPlayerPause();
    public void  onPlayerPlay();
    public void  onPlayerPrevious();
    public void  onPlayerNext();
}
