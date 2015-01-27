package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Toast;


import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import ee.promobox.promoboxandroid.util.ToastIntent;
import ee.promobox.promoboxandroid.widgets.AspectRatioImageView;

public class ImageActivity extends Activity {

    private final String IMAGE_ACTIVITY_STRING = "ImageActivity ";

    private ImageView slide;
    private LocalBroadcastManager bManager;
    private ArrayList<CampaignFile> files;
    private int position = 0;
    private boolean active = true;
    private int orientation;
    private long delay = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(IMAGE_ACTIVITY_STRING, "onCreate");

        setContentView(R.layout.activity_image);

        bManager = LocalBroadcastManager.getInstance(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.ACTIVITY_FINISH);
        bManager.registerReceiver(bReceiver, intentFilter);

        slide = (ImageView) findViewById(R.id.slide_1);

        Bundle extras = getIntent().getExtras();

        files = extras.getParcelableArrayList("files");
        delay = getIntent().getExtras().getInt("delay") * 1000;

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(IMAGE_ACTIVITY_STRING, "onResume");

        active = true;

        Log.i(IMAGE_ACTIVITY_STRING, "Orientation: " + getIntent().getExtras().getInt("orientation"));
        orientation = getIntent().getExtras().getInt("orientation");

        if (orientation == MainActivity.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        hideSystemUI();

        if (position > files.size() - 1) {
            position = 0;
        }

        if (files.size() > 0) {
            playImage(files.get(position).getPath());
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        active = false;
    }

    private Bitmap decodeBitmap(File file) {
        Bitmap bm = null;

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();

            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inDither = false;
            options.inTempStorage = new byte[32 * 1024];

            bm = BitmapFactory.decodeFile(file.getPath(), options);


        } catch (Exception ex) {
            Log.e(IMAGE_ACTIVITY_STRING, ex.getMessage(), ex);
            Toast.makeText(this,ex.toString(), Toast.LENGTH_SHORT).show();
        }

        return bm;
    }

    public Bitmap rotateBitmap(Bitmap source, float angle) {
        try {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            Bitmap newBitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
            source.recycle();
            return newBitmap;
        } catch (Exception e){
            Log.e(IMAGE_ACTIVITY_STRING, e.getMessage(), e);
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            return source;
        }
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

    final Runnable r = new Runnable() {
        @Override
        public void run() {
            if (position == files.size() || !active) {

                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", MainActivity.RESULT_FINISH_PLAY);
                setResult(RESULT_OK, returnIntent);

                ImageActivity.this.finish();
            } else {
                playImage(files.get(position).getPath());
            }
        }
    };

    private void sendPlayCampaignFile() {
        Intent playFile = new Intent(MainActivity.CURRENT_FILE_ID);
        playFile.putExtra("fileId", files.get(position).getId());
        Log.d(IMAGE_ACTIVITY_STRING, files.get(position).getPath());
        bManager.sendBroadcast(playFile);
    }

    private void playImage(String path) {
        File file = new File(path);
        if (!file.exists()){
            Log.e(IMAGE_ACTIVITY_STRING, "No file in path " + path);
            Toast.makeText(this,"No file in path " + path, Toast.LENGTH_SHORT).show();
            position ++ ;
            slide.postDelayed(r, delay);
            return;
        }
        try {
            Bitmap bitmap = decodeBitmap(file);
            if (orientation == MainActivity.ORIENTATION_PORTRAIT_EMULATION) {
                bitmap = rotateBitmap(bitmap, 270);
            }
            recycleBitmap();
            slide.setImageBitmap(bitmap);

            sendPlayCampaignFile();

            position++;

            slide.postDelayed(r, delay);


        } catch (Exception ex) {
            Log.e(IMAGE_ACTIVITY_STRING, ex.getMessage(), ex);
            Log.e(IMAGE_ACTIVITY_STRING, "Path = " + path );

            bManager.sendBroadcast(new ToastIntent(ex.toString()));

            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", MainActivity.RESULT_FINISH_PLAY);
            setResult(RESULT_OK, returnIntent);

            ImageActivity.this.finish();
        }

    }


    @Override
    protected void onDestroy() {
        recycleBitmap();

        slide.destroyDrawingCache();
        slide = null;

        bManager.unregisterReceiver(bReceiver);
        super.onDestroy();
    }

    private void recycleBitmap() {
        BitmapDrawable toRecycle = (BitmapDrawable) slide.getDrawable();

        if (toRecycle != null && toRecycle.getBitmap() != null) {
            toRecycle.getBitmap().recycle();
        }
    }


    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        private final String RECEIVER_STRING = IMAGE_ACTIVITY_STRING + "BroadcastReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MainActivity.ACTIVITY_FINISH)) {
                Intent returnIntent = new Intent();

                returnIntent.putExtra("result", MainActivity.RESULT_FINISH_PLAY);
                ImageActivity.this.setResult(RESULT_OK, returnIntent);

                ImageActivity.this.finish();
            }
        }
    };
}
