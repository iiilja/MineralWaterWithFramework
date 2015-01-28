package ee.promobox.promoboxandroid.intents;

import android.content.Intent;

import ee.promobox.promoboxandroid.MainActivity;
import ee.promobox.promoboxandroid.data.ErrorMessage;

/**
 * Created by ilja on 28.01.2015.
 */
public class ErrorMessageIntent extends Intent {

    public ErrorMessageIntent(String name, String message, StackTraceElement[] stackTrace){
        super(MainActivity.ADD_ERROR_MSG);
        putExtra("message", new ErrorMessage(name , message, stackTrace));
    }

    public ErrorMessageIntent(Exception ex){
        super(MainActivity.ADD_ERROR_MSG);
        String className = ex.getClass().getSimpleName();
        ErrorMessage message = new ErrorMessage(className,ex.getLocalizedMessage(),ex.getStackTrace());
        putExtra("message", message);
    }
}
