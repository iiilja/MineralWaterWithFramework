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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
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

                Toast.makeText(view.getContext(), "Just a test", Toast.LENGTH_SHORT).show();

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
        intentFilter.addAction(MainActivity.NO_NETWORK);

        bManager.registerReceiver(bReceiver, intentFilter);

        Bundle extras = getIntent().getExtras();

        files = extras.getParcelableArrayList("files");

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

        if (files.size() > 0) {
            playAudio();
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

    private void playAudio() {
        cleanUp();
        String pathToFile = files.get(position).getPath();
        Log.d(AUDIO_ACTIVITY,"playAudio() file = " + new File(pathToFile).getName());
        Uri uri = Uri.parse(pathToFile);
        final SampleSource source;
        source = new FrameworkSampleSource(this,uri,null,1);
        audioRenderer = new MediaCodecAudioTrackRenderer(
                source, null, true);
        exoPlayer = ExoPlayer.Factory.newInstance(1,0,0);
        exoPlayer.prepare(audioRenderer);
        exoPlayer.setPlayWhenReady(true);
        sendPlayCampaignFile();
        exoPlayer.addListener(new OnTrackFinished());
        position++;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int counter = 0;
                while(exoPlayer != null && exoPlayer.getPlaybackState()<4 && counter < 10){
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d(AUDIO_ACTIVITY,"---------------------------------");
                    Log.d(AUDIO_ACTIVITY," PLAYBACK_STATE :\t" + exoPlayer.getPlaybackState());
                    Log.d(AUDIO_ACTIVITY, "---------------------------------");
                    counter++;

                }
            }
        });
        thread.start();


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

                if (position == files.size()) {
                    cleanUp();

                    Intent returnIntent = new Intent();

                    returnIntent.putExtra("result", 1);

                    AudioActivity.this.setResult(RESULT_OK, returnIntent);

                    AudioActivity.this.finish();
                }
                else {
                    playAudio();
                }
            }
        }

        @Override
        public void onPlayWhenReadyCommitted() {

        }

        @Override
        public void onPlayerError(ExoPlaybackException e) {

        }
    }


    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        private final String RECEIVER_STRING = AUDIO_ACTIVITY + "BroadcastReceiver";
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MainActivity.ACTIVITY_FINISH)) {
                cleanUp();
                Intent returnIntent = new Intent();

                returnIntent.putExtra("result", MainActivity.RESULT_FINISH_PLAY);

                AudioActivity.this.setResult(RESULT_OK, returnIntent);

                AudioActivity.this.finish();
            } else if (action.equals(MainActivity.NO_NETWORK)){
                Log.d(RECEIVER_STRING, "NO NETWORK");
                try {
                    new NoNetworkDialog().show(getFragmentManager(),"NO_NETWORK");
                } catch (IllegalStateException ignored){
                }
            }
        }
    };

}
