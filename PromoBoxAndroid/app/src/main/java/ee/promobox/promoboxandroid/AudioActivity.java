package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.media.MediaExtractor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.FrameworkSampleSource;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.SampleSource;

import java.io.File;
import java.util.ArrayList;


//https://github.com/felixpalmer/android-visualizer
public class AudioActivity extends Activity {

    private final String AUDIO_ACTIVITY = "AudioActivity ";

    ExoPlayer exoPlayer;
    MediaCodecAudioTrackRenderer audioRenderer;

    private SampleSource source;

    private LocalBroadcastManager bManager;

    private ArrayList<CampaignFile> files;
    private int position = 0;

    private void hideSystemUI() {

        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
        );


        View view = findViewById(R.id.audio_view);

        view.setOnLongClickListener(new View.OnLongClickListener() {

            public boolean onLongClick(View view) {
                Intent i = new Intent(AudioActivity.this, SettingsActivity.class);
                startActivity(i);
                return true;
            }
        });
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

        files = extras.getParcelableArrayList("files");

    }

    @Override
    protected void onResume() {
        super.onResume();

        hideSystemUI();

        int orientation = getIntent().getExtras().getInt("orientation");
        if ( orientation == MainActivity.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if ( orientation == MainActivity.ORIENTATION_PORTRAIT_EMULATION){
            findViewById(R.id.audio_view).setRotation(270);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        if (position > files.size() - 1) {
            position = 0;
        }

        if (files.size() > 0) {
            try {
                playAudio();
            } catch (Exception e) {
                Log.e(AUDIO_ACTIVITY, "onResume " + e.getMessage());
                Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
                tryNextFile();
            }
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

    private void sendPlayCampaignFile() {
        Intent playFile = new Intent(MainActivity.CURRENT_FILE_ID);
        playFile.putExtra("fileId", files.get(position).getId());
        LocalBroadcastManager.getInstance(AudioActivity.this).sendBroadcast(playFile);
    }

    private void playAudio() throws Exception{
        cleanUp();
        String pathToFile = files.get(position).getPath();
        File file = new File(pathToFile);
        if (!file.exists()){
            Log.e(AUDIO_ACTIVITY, "File not found : " + pathToFile);
            throw new Exception("File not found : " + pathToFile);
        }
        Log.d(AUDIO_ACTIVITY,"playAudio() file = " + file.getName() + " PATH = " + pathToFile);
        setStatus(files.get(position).getName());
        Uri uri = Uri.parse(pathToFile);
        source = new FrameworkSampleSource(this,uri,null,1);
        audioRenderer = new MediaCodecAudioTrackRenderer(
                source, null, true);
        exoPlayer = ExoPlayer.Factory.newInstance(1);
        exoPlayer.prepare(audioRenderer);
        exoPlayer.setPlayWhenReady(true);
        sendPlayCampaignFile();
        exoPlayer.addListener(new OnTrackFinished());
    }

    private void cleanUp() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        audioRenderer = null;

    }

    private class OnTrackFinished implements ExoPlayer.Listener {

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

            if (playbackState == ExoPlayer.STATE_ENDED) {

                if (position + 1 == files.size()) {
                    finishActivity();
                }
                else {
                    tryNextFile();
                }
            }
        }

        @Override
        public void onPlayWhenReadyCommitted() {

        }

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            Toast.makeText(AudioActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            Log.e(AUDIO_ACTIVITY, "onPlayerError " + e.getMessage());
            tryNextFile();
        }
    }

    private void finishActivity (){
        cleanUp();
        AudioActivity.this.setResult(RESULT_OK);
        AudioActivity.this.finish();
    }

    private void tryNextFile(){
        try {
            position ++;
            if (position < files.size()){
                playAudio();
            } else {
                Toast.makeText(this,"Player error",Toast.LENGTH_LONG).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finishActivity();
                    }
                }, 1000);
            }
        }
        catch (Exception ex){
            Log.e(AUDIO_ACTIVITY, "onPlayerError " + ex.getMessage());
            Toast.makeText(this,ex.getMessage(),Toast.LENGTH_SHORT).show();
            tryNextFile();
        }
    }

    private void setStatus(String status){
        TextView textView = (TextView)findViewById(R.id.audio_activity_status);
        textView.setText(status);
    }


    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        private final String RECEIVER_STRING = AUDIO_ACTIVITY + "BroadcastReceiver";
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MainActivity.ACTIVITY_FINISH)) {
                finishActivity();
            }
        }
    };

}
