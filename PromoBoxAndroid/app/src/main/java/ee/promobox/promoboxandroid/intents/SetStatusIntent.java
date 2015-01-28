package ee.promobox.promoboxandroid.intents;

import android.content.Intent;

import ee.promobox.promoboxandroid.MainActivity;

/**
 * Created by ilja on 26.01.2015.
 */
public class SetStatusIntent extends Intent {
    public SetStatusIntent(String status){
        super(MainActivity.SET_STATUS);
        super.putExtra("status", status);
    }
}
