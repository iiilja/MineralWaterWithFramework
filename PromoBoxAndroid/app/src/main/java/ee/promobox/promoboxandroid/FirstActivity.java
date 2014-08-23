package ee.promobox.promoboxandroid;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class FirstActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_first);

        Button b = (Button)findViewById(R.id.first_start_button);
        final EditText et = (EditText)findViewById(R.id.first_start_device_id);

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


}
