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

import ee.promobox.promoboxandroid.util.SoundFadeAnimation;

//https://github.com/felixpalmer/android-visualizer
public class AudioActivity extends Activity {

    private MediaPlayer mPlayer;
    private SoundFadeAnimation soundFadeAnimation;
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

        bManager.registerReceiver(bReceiver, intentFilter);

        Bundle extras = getIntent().getExtras();

        paths = extras.getStringArray("paths");
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

        if (position > paths.length - 1) {
            position = 0;
        }

        if (paths.length > 0) {
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

    private void playAudio() {
        mPlayer = new MediaPlayer();

        try {
            inputStream = new FileInputStream(paths[position]);
            Log.d("AudioActivity","Create stream: " + inputStream);
            mPlayer.setDataSource(inputStream.getFD());
            mPlayer.prepare();
            mPlayer.start();

            soundFadeAnimation = new SoundFadeAnimation(mPlayer);

            soundFadeAnimation.setOnFadeCallback(new SoundFadeAnimation.FadeCallback() {
                @Override
                public void onFade() {
                    if (position != paths.length) {
                        playAudio();
                    }
                }
            });

            mPlayer.setOnCompletionListener(new OnTrackFinished(inputStream));
            position++;

        } catch (Exception ex) {
            Log.e("AudioActivity", ex.getMessage(), ex);
            IOUtils.closeQuietly(inputStream);

            Intent returnIntent = new Intent();

            returnIntent.putExtra("result", MainActivity.RESULT_FINISH_PLAY);

            AudioActivity.this.setResult(RESULT_OK, returnIntent);

            AudioActivity.this.finish();
        }

    }

    private void cleanUp() {
        if (mPlayer != null) {
            mPlayer.setOnCompletionListener(null);
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }

        if(soundFadeAnimation != null) {
            soundFadeAnimation.setOnFadeCallback(null);
            soundFadeAnimation.cleanUp();
            soundFadeAnimation = null;
        }

        IOUtils.closeQuietly(inputStream);
    }


    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MainActivity.ACTIVITY_FINISH)) {
                cleanUp();
                Intent returnIntent = new Intent();

                returnIntent.putExtra("result", MainActivity.RESULT_FINISH_PLAY);

                AudioActivity.this.setResult(RESULT_OK, returnIntent);

                AudioActivity.this.finish();
            }
        }
    };


    private class OnTrackFinished implements MediaPlayer.OnCompletionListener {

        private  FileInputStream iStream;

        public OnTrackFinished(FileInputStream stream) {
            this.iStream = stream;
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            IOUtils.closeQuietly(iStream);
            Log.d("AudioActivity","Close stream: " + iStream);
            if (position == paths.length) {
                cleanUp();

                Intent returnIntent = new Intent();

                returnIntent.putExtra("result", 1);

                AudioActivity.this.setResult(RESULT_OK, returnIntent);

                AudioActivity.this.finish();
            }
        }
    }

}
