package ee.promobox.promoboxandroid;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by Maxim on 10.07.2014.
 */
public class MyScheduleReceiver extends BroadcastReceiver {

    private static final int REPEAT_TIME_SECONDS = 60;
    public static final long REPEAT_TIME = 1000 * REPEAT_TIME_SECONDS;

    @Override
    public void onReceive(Context context, Intent intent) {


        AlarmManager service = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);

        Intent i = new Intent(context, MyStartServiceReceiver.class);

        String action = intent.getAction();

        if (action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals(MainActivity.UI_RESURRECT) ) {
            Intent mainService = new Intent(context, MainService.class);
            mainService.putExtra("startMainActivity", true);
            context.startService(mainService);
        } else if (action.equals(MainActivity.APP_START) ) {
            Intent mainService = new Intent(context, MainService.class);
            context.startService(mainService);
        }

        PendingIntent pending = PendingIntent.getBroadcast(context, 0, i,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Calendar cal = Calendar.getInstance();

        cal.add(Calendar.SECOND, REPEAT_TIME_SECONDS);

        Log.i("MyScheduleReceiver", "Started fetch every "+REPEAT_TIME_SECONDS+" seconds");

        service.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                cal.getTimeInMillis(), REPEAT_TIME, pending);


    }
}
