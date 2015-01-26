package ee.promobox.promoboxandroid;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class FirstActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_first);

        Button b = (Button)findViewById(R.id.first_start_button);
        final EditText et = (EditText)findViewById(R.id.first_start_device_id);

        IntentFilter intentFilter = new IntentFilter();


        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        intentFilter.addAction(MainActivity.NO_NETWORK);
        intentFilter.addAction(MainActivity.MAKE_TOAST);
        bManager.registerReceiver(bReceiver, intentFilter);


        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("FirstActivity", et.getText().toString());

                Intent returnIntent = new Intent();

                returnIntent.putExtra("result", MainActivity.RESULT_FINISH_FIRST_START);

                //Nado proverjat
                returnIntent.putExtra("deviceUuid", et.getText().toString());

                setResult(RESULT_OK, returnIntent);

                FirstActivity.this.finish();
            }
        });
    }

    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        private final String RECEIVER_STRING = "FirstActivity" + "BroadcastReceiver";
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MainActivity.NO_NETWORK)){
                Log.d(RECEIVER_STRING, "NO NETWORK");
                try {
                    new NoNetworkDialog().show(getFragmentManager(),"NO_NETWORK");
                } catch (IllegalStateException ex){
                }
            } else if (action.equals(MainActivity.MAKE_TOAST)){
                Log.d(RECEIVER_STRING, "Make TOAST");
                Toast.makeText(getApplicationContext(),intent.getStringExtra("Toast"), Toast.LENGTH_LONG).show();
            }
        }
    };


}
