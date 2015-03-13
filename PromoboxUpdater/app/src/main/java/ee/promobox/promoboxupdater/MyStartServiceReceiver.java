package ee.promobox.promoboxupdater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Maxim on 10.07.2014.
 */
public class MyStartServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("MyStartServiceReceiver", "Main service starting");

        Intent service = new Intent(context, MainService.class);
        context.startService(service);
    }
}
