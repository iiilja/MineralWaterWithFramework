package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;

//http://cadabracorp.com/blog/2013/04/24/playing-a-full-screen-video-the-easy-way/
public class VideoActivity extends Activity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, SurfaceHolder.Callback {


    private SurfaceView videoView;
    private LocalBroadcastManager bManager;
    private String[] paths;
    private int position = 0;
    private MediaPlayer mediaPlayer;
    private FileInputStream streamMediaPlayer;
    private SurfaceHolder surfaceHolder;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        setContentView(R.layout.activity_video);

        bManager = LocalBroadcastManager.getInstance(this);

        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(MainActivity.ACTIVITY_FINISH);

        bManager.registerReceiver(bReceiver, intentFilter);

        Bundle extras = getIntent().getExtras();

        paths = extras.getStringArray("paths");

        videoView = (SurfaceView) findViewById(R.id.videoview);

        surfaceHolder = videoView.getHolder();

        surfaceHolder.addCallback(this);

    }


    public void playVideo() {
        try {

            mediaPlayer = new MediaPlayer();

            mediaPlayer.setDisplay(surfaceHolder);

            streamMediaPlayer = new FileInputStream(paths[position]);

            mediaPlayer.setDataSource(streamMediaPlayer.getFD());

            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(this);

            position++;

            mediaPlayer.setOnCompletionListener(this);

        } catch (Exception ex) {
            Log.e("VideoActivity", ex.getMessage(), ex);
            IOUtils.closeQuietly(streamMediaPlayer);
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

        View view = findViewById(R.id.video_view);

        view.setOnLongClickListener(new View.OnLongClickListener() {

            public boolean onLongClick(View view) {
                Toast.makeText(view.getContext(), "Just a test", Toast.LENGTH_SHORT).show();

                return true;
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        hideSystemUI();

        if (getIntent().getExtras().getInt("orintation") == MainActivity.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        if (position > paths.length - 1) {
            position = 0;
        }

    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        if (position < paths.length) {
            mediaPlayer.setOnCompletionListener(null);
            mediaPlayer.release();

            IOUtils.closeQuietly(streamMediaPlayer);

            playVideo();
        } else {

            Intent returnIntent = new Intent();

            returnIntent.putExtra("result", MainActivity.RESULT_FINISH_PLAY);

            setResult(RESULT_OK, returnIntent);

            VideoActivity.this.finish();
        }
    }


    @Override
    protected void onDestroy() {
        videoView.destroyDrawingCache();

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        IOUtils.closeQuietly(streamMediaPlayer);

        bManager.unregisterReceiver(bReceiver);

        super.onDestroy();
    }


    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MainActivity.ACTIVITY_FINISH)) {
                VideoActivity.this.finish();
            }
        }
    };


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        playVideo();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }
}
