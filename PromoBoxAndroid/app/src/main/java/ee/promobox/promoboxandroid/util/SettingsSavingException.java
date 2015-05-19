package ee.promobox.promoboxandroid.util;

/**
 * Created by ilja on 13.05.2015.
 */
public class SettingsSavingException extends Exception {
    private static final String MESSAGE = "Could not save settings because of %s";

    public SettingsSavingException (Exception e){
        super(String.format(MESSAGE, e.getMessage()));
    }
}
