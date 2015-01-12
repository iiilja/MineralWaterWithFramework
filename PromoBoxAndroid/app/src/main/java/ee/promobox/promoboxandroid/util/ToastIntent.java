package ee.promobox.promoboxandroid.util;

import android.content.Intent;

/**
 * Created by ilja on 8.01.2015.
 */
public class ToastIntent extends Intent {
    
    public ToastIntent(String toastString){
        super.putExtra("Toast",toastString);
    }
}
