package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;

import com.pheelicks.visualizer.VisualizerView;
import com.pheelicks.visualizer.renderer.CircleBarRenderer;

import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;

//https://github.com/felixpalmer/android-visualizer
public class AudioActivity extends Activity {

    private MediaPlayer mPlayer;
    private VisualizerView mVisualizerView;
    private LocalBroadcastManager bManager;
    private String[] paths;
    private int position = 0;
    private FileInputStream inputStream;

    private void hideSystemUI() {

        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_audio);

        bManager = LocalBroadcastManager.getInstance(this);

        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(MainActivity.ACTIVITY_FINISH);

        bManager.registerReceiver(bReceiver, intentFilter);

        Bundle extras = getIntent().getExtras();

        paths = extras.getStringArray("paths");
    }

    @Override
    protected void onResume() {
        super.onResume();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        hideSystemUI();

        try {
            playAudio();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        cleanUp();

        bManager.unregisterReceiver(bReceiver);

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cleanUp();
    }

    private void playAudio() {
        mPlayer = new MediaPlayer();

        try {
            inputStream = new FileInputStream(paths[position]);

            mPlayer.setDataSource(inputStream.getFD());
            mPlayer.prepare();

            mVisualizerView = (VisualizerView) findViewById(R.id.visualizerView);
            mVisualizerView.link(mPlayer);

            addCircleBarRenderer();

            mPlayer.start();

            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    cleanUp();

                    if (position == paths.length) {

                        Intent returnIntent = new Intent();

                        returnIntent.putExtra("result", 1);

                        AudioActivity.this.setResult(RESULT_OK, returnIntent);

                        AudioActivity.this.finish();
                    } else {
                        playAudio();
                    }
                }
            });

            position++;

        } catch (Exception ex) {
            Log.e("AudioActivity", ex.getMessage(), ex);
            position++;
            playAudio();
        }

    }




    private void cleanUp() {
        if (mPlayer != null) {
            mVisualizerView.release();
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }

        IOUtils.closeQuietly(inputStream);

    }

    private void addCircleBarRenderer() {
        Paint paint = new Paint();
        paint.setStrokeWidth(8f);
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN));
        paint.setColor(Color.argb(255, 222, 92, 143));
        CircleBarRenderer circleBarRenderer = new CircleBarRenderer(paint, 32, true);
        mVisualizerView.addRenderer(circleBarRenderer);
    }

    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MainActivity.ACTIVITY_FINISH)) {
                cleanUp();
                AudioActivity.this.finish();
            }
        }
    };


}
