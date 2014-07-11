package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

//http://thebitplague.wordpress.com/2013/04/05/kiosk-mode-on-the-nexus-7/
//http://www.tutorialspoint.com/android/android_imageswitcher.htm
public class MainActivity extends Activity {

    private MainService mainService;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Intent i = new Intent(this, ImageActivity.class);

        i.putExtra("images", new int[] {R.drawable.test, R.drawable.test2});

        startActivityForResult(i, 1);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent= new Intent(this, MainService.class);
        bindService(intent, mConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
    }


    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {
            MainService.MyBinder b = (MainService.MyBinder) binder;
            mainService = b.getService();
            Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT)
                    .show();
        }

        public void onServiceDisconnected(ComponentName className) {
            mainService = null;
        }
    };






}
