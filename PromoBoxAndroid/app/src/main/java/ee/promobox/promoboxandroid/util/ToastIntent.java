package ee.promobox.promoboxandroid.util;

import android.content.Intent;

import ee.promobox.promoboxandroid.MainActivity;

/**
 * Created by ilja on 8.01.2015.
 */
public class ToastIntent extends Intent {
    
    public ToastIntent(String toastString){
        super(MainActivity.MAKE_TOAST);
        super.putExtra("Toast",toastString);

    }
}
