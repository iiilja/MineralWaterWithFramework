package ee.promobox.promoboxandroid.intents;

import android.content.Intent;

import ee.promobox.promoboxandroid.MainActivity;
import ee.promobox.promoboxandroid.util.StatusEnum;

/**
 * Created by ilja on 26.01.2015.
 */
public class SetStatusIntent extends Intent {
    public SetStatusIntent(StatusEnum statusEnum, String status){
        super(MainActivity.SET_STATUS);
        super.putExtra("status", status);
        super.putExtra("statusEnum", statusEnum);
    }
}
