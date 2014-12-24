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
import java.util.ArrayList;


public class VideoActivity extends Activity implements MediaPlayer.OnCompletionListener, SurfaceHolder.Callback {


    private SurfaceView videoView;
    private LocalBroadcastManager bManager;
    private ArrayList<CampaignFile> files;
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

        files = extras.getParcelableArrayList("files");

        videoView = (SurfaceView) findViewById(R.id.videoview);

    }


    public void playVideo() {
        if (files.size() > 0) {
            try {

                mediaPlayer = new MediaPlayer();

                mediaPlayer.setDisplay(surfaceHolder);

                mediaPlayer.setScreenOnWhilePlaying(true);

                streamMediaPlayer = new FileInputStream(files.get(position).getPath());

                mediaPlayer.setDataSource(streamMediaPlayer.getFD());

                mediaPlayer.prepare();

                mediaPlayer.start();

                sendPlayCampaignFile();

                position++;

                mediaPlayer.setOnCompletionListener(this);

            } catch (Exception ex) {
                Log.e("VideoActivity", ex.getMessage(), ex);

                Intent returnIntent = new Intent();

                returnIntent.putExtra("result", MainActivity.RESULT_FINISH_PLAY);

                VideoActivity.this.setResult(RESULT_OK, returnIntent);

                VideoActivity.this.finish();
            }
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

                Intent i = new Intent(VideoActivity.this, SettingsActivity.class);
                startActivity(i);

                Toast.makeText(view.getContext(), "Just a test", Toast.LENGTH_SHORT).show();

                return true;
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        hideSystemUI();

        if (getIntent().getExtras().getInt("orientation") == MainActivity.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        if (position > files.size() - 1) {
            position = 0;
        }

        surfaceHolder = videoView.getHolder();

        surfaceHolder.addCallback(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        cleanUp();

    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        if (position < files.size()) {

            mediaPlayer.setOnCompletionListener(null);
            mediaPlayer.stop();
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


    private void cleanUp() {
        videoView.destroyDrawingCache();

        surfaceHolder = videoView.getHolder();

        surfaceHolder.removeCallback(this);

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.setOnCompletionListener(null);
            mediaPlayer.release();
            mediaPlayer = null;
        }

        IOUtils.closeQuietly(streamMediaPlayer);
    }

    private void sendPlayCampaignFile() {
        Intent playFile = new Intent(MainActivity.CURRENT_FILE_ID);
        playFile.putExtra("fileId", files.get(position).getId());
        LocalBroadcastManager.getInstance(VideoActivity.this).sendBroadcast(playFile);
    }

    @Override
    protected void onDestroy() {
        cleanUp();

        bManager.unregisterReceiver(bReceiver);

        super.onDestroy();
    }


    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MainActivity.ACTIVITY_FINISH)) {
                Intent returnIntent = new Intent();

                returnIntent.putExtra("result", MainActivity.RESULT_FINISH_PLAY);

                VideoActivity.this.setResult(RESULT_OK, returnIntent);

                VideoActivity.this.finish();
            }
        }
    };


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.i("VideoActivity", "surfaceCreated");
        playVideo();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.i("VideoActivity", "surfaceDestroyed");
    }

}
