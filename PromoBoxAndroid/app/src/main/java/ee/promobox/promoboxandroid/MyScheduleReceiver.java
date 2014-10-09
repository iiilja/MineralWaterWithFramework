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


    private static final long REPEAT_TIME = 1000 * 30;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Process process = Runtime.getRuntime().exec("openvpn --config /data/local/openvpn/c1.ovpn &");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        AlarmManager service = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);

        Intent i = new Intent(context, MyStartServiceReceiver.class);

        PendingIntent pending = PendingIntent.getBroadcast(context, 0, i,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Calendar cal = Calendar.getInstance();

        cal.add(Calendar.SECOND, 30);

        Log.i("MyScheduleReceiver", "Started fetch every 30 seconds");

        service.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                cal.getTimeInMillis(), REPEAT_TIME, pending);


    }
}
