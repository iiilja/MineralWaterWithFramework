package ee.promobox.promoboxandroid.util;

import android.content.Context;
import android.content.Intent;

import java.util.TimerTask;

import ee.promobox.promoboxandroid.MainService;

/**
 * Created by ilja on 24.04.2015.
 */
public class MainServiceTimerTask extends TimerTask {
    private Context context;

    public MainServiceTimerTask(Context context){
        this.context = context;
    }

    @Override
    public void run() {
        Intent service = new Intent(context, MainService.class);
        context.startService(service);
    }
}
