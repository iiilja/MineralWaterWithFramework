package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;

public class ImageActivity extends Activity {

    private ImageView slide;
    private LocalBroadcastManager bManager;
    private String[] paths;
    private int position = 0;

    private Bitmap decodeBitmap(File file) {
        Bitmap bm = null;

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();

            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inDither = false;
            options.inTempStorage = new byte[32 * 1024];

            FileInputStream fs = new FileInputStream(file);

            bm = BitmapFactory.decodeFileDescriptor(fs.getFD(), null, options);

            fs.getFD().sync();

            IOUtils.closeQuietly(fs);


        } catch (Exception ex) {
            Log.e("ImageActivity", ex.getMessage(), ex);
        }

        return bm;
    }

    private void hideSystemUI() {

        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
        );



        View view = findViewById(R.id.image_view);

        view.setOnLongClickListener(new View.OnLongClickListener() {

            public boolean onLongClick(View view) {
                Intent i = new Intent(ImageActivity.this, SettingsActivity.class);
                startActivity(i);

                return true;
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();

        Log.i("ImageActivity", "Orientation: " + getIntent().getExtras().getInt("orientation"));

        if (getIntent().getExtras().getInt("orientation") == MainActivity.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        hideSystemUI();

        if (position > paths.length - 1) {
            position = 0;
        }

        if (paths.length > 0) {
            playImage(paths[position]);
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image);

        bManager = LocalBroadcastManager.getInstance(this);

        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(MainActivity.ACTIVITY_FINISH);

        bManager.registerReceiver(bReceiver, intentFilter);

        slide = (ImageView) findViewById(R.id.slide_1);

        Bundle extras = getIntent().getExtras();

        paths = extras.getStringArray("paths");

    }

    final Runnable r = new Runnable() {
        @Override
        public void run() {
            if (position == paths.length) {

                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", MainActivity.RESULT_FINISH_PLAY);
                setResult(RESULT_OK, returnIntent);

                ImageActivity.this.finish();
            } else {
                playImage(paths[position]);
            }
        }
    };

    private void playImage(String path) {
        File file = new File(path);

        try {

            slide.setImageBitmap(decodeBitmap(file));

            position++;

            final long delay = getIntent().getExtras().getInt("delay") * 1000;

            slide.postDelayed(r, delay);

        } catch (Exception ex) {
            Log.e("ImageActivity", ex.getMessage(), ex);

            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", MainActivity.RESULT_FINISH_PLAY);
            setResult(RESULT_OK, returnIntent);

            ImageActivity.this.finish();
        }

    }


    @Override
    protected void onDestroy() {
        BitmapDrawable toRecycle = (BitmapDrawable)slide.getDrawable();

        if (toRecycle != null && toRecycle.getBitmap() != null) {
            toRecycle.getBitmap().recycle();
        }

        slide.destroyDrawingCache();
        slide = null;

        bManager.unregisterReceiver(bReceiver);

        super.onDestroy();
    }




    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MainActivity.ACTIVITY_FINISH)) {
                Intent returnIntent = new Intent();

                returnIntent.putExtra("result", MainActivity.RESULT_FINISH_PLAY);
                ImageActivity.this.setResult(RESULT_OK, returnIntent);

                ImageActivity.this.finish();
            }
        }
    };
}
