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
import android.view.View;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.util.ArrayList;

import ee.promobox.promoboxandroid.util.SoundFadeAnimation;
import ee.promobox.promoboxandroid.util.ToastIntent;

//https://github.com/felixpalmer/android-visualizer
public class AudioActivity extends Activity {

    private final String AUDIO_ACTIVITY = "AudioActivity ";

    private MediaPlayer mPlayer;
    private MediaPlayer previousPlayer;

    private SoundFadeAnimation soundFadeAnimation;
    private SoundFadeAnimation previousFadeAnimation;

    private FileInputStream inputStream;
    private FileInputStream previousInputStream;

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
        previousPlayer = mPlayer;
        mPlayer = new MediaPlayer();

        try {
            previousInputStream = inputStream;
            inputStream = new FileInputStream(files.get(position).getPath());

            mPlayer.setDataSource(inputStream.getFD());
            mPlayer.prepare();
            mPlayer.start();

            sendPlayCampaignFile();

            previousFadeAnimation = soundFadeAnimation;
            soundFadeAnimation = new SoundFadeAnimation(mPlayer);
            soundFadeAnimation.setOnFadeCallback(new SoundFadeAnimation.FadeCallback() {
                @Override
                public void onFade() {
                    if (position != files.size()) {
                        playAudio();
                    }
                }
            });

            mPlayer.setOnCompletionListener(new OnTrackFinished(inputStream, soundFadeAnimation));
            position++;

        } catch (Exception ex) {
            Log.e(AUDIO_ACTIVITY, ex.getMessage(), ex);
            Intent returnIntent = new Intent();

            returnIntent.putExtra("result", MainActivity.RESULT_FINISH_PLAY);

            AudioActivity.this.setResult(RESULT_OK, returnIntent);

            AudioActivity.this.finish();
        }

    }

    private void cleanUp() {
        if (mPlayer != null) {
            clearMediaPlayer(mPlayer);
            mPlayer = null;
        }

        if (previousPlayer != null) {
            clearMediaPlayer(previousPlayer);
            previousPlayer = null;
        }

        if (soundFadeAnimation != null) {
            clearFadeAnimation(soundFadeAnimation);
            soundFadeAnimation = null;
        }

        if (previousFadeAnimation != null) {
            clearFadeAnimation(previousFadeAnimation);
            previousFadeAnimation = null;
        }

        if (inputStream != null) {
            IOUtils.closeQuietly(inputStream);
        }

        if (previousInputStream != null) {
            IOUtils.closeQuietly(previousInputStream);
        }
    }

    private void clearMediaPlayer(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(null);
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    private void clearFadeAnimation(SoundFadeAnimation fadeAnimation) {
        if (fadeAnimation != null) {
            fadeAnimation.cleanUp();
            fadeAnimation.setOnFadeCallback(null);
        }
    }


    private class OnTrackFinished implements MediaPlayer.OnCompletionListener {

        private FileInputStream iStream;
        private SoundFadeAnimation fadeAnimation;
        public OnTrackFinished(FileInputStream stream, SoundFadeAnimation fadeAnimation) {
            this.iStream = stream;
            this.fadeAnimation = fadeAnimation;
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            clearMediaPlayer(mp);
            clearFadeAnimation(fadeAnimation);
            IOUtils.closeQuietly(iStream);

            if (position == files.size()) {
                cleanUp();

                Intent returnIntent = new Intent();

                returnIntent.putExtra("result", 1);

                AudioActivity.this.setResult(RESULT_OK, returnIntent);

                AudioActivity.this.finish();
            }
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
                    DownloadFilesTask.getNoNetworkDialogFragment().show(getFragmentManager(),"NO_NETWORK");
                } catch (IllegalStateException ex){
                }
            }
        }
    };

}
