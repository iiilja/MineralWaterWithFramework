package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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


        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uuid = String.valueOf(et.getText());
                Log.i("FirstActivity", uuid);

                if ( uuid == null || uuid.equals("")){
                    Toast.makeText(FirstActivity.this, "Wrong uuid", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", MainActivity.RESULT_FINISH_FIRST_START);
                returnIntent.putExtra("deviceUuid", et.getText().toString());
                setResult(RESULT_OK, returnIntent);
                FirstActivity.this.finish();
            }
        });
    }




}
