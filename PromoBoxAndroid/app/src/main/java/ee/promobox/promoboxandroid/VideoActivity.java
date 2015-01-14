package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.MediaController;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.FrameworkSampleSource;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.SampleSource;
import com.google.android.exoplayer.util.PlayerControl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import ee.promobox.promoboxandroid.util.ToastIntent;


public class VideoActivity extends Activity implements TextureView.SurfaceTextureListener {
    private final String VIDEO_ACTIVITY = "VideoActivity ";

    private TextureView videoView;
    private LocalBroadcastManager bManager;
    private ArrayList<CampaignFile> files;
    private int position = 0;
    private boolean active = true;

    private ExoPlayer exoPlayer;
    private MediaCodecAudioTrackRenderer audioRenderer;
    private MediaCodecVideoTrackRenderer videoRenderer;

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

    }


    public void playVideo() {
        if (files.size() > 0) {
            try {
                cleanUp();
                Surface surface = new Surface(videoView.getSurfaceTexture());
                String pathToFile = files.get(position).getPath();
                Log.d(VIDEO_ACTIVITY,"playVideo() file = " + new File(pathToFile).getName());
                Uri uri = Uri.parse(pathToFile);
                SampleSource source;
                source = new FrameworkSampleSource(this,uri,null,2);
                audioRenderer = new MediaCodecAudioTrackRenderer(
                        source, null, true);
                videoRenderer = new MediaCodecVideoTrackRenderer(source,
                        MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 0, new Handler(getMainLooper()),
                        new MediaCodecVideoTrackRendererEventListener(), 50);
                exoPlayer = ExoPlayer.Factory.newInstance(2);
                exoPlayer.prepare(videoRenderer,audioRenderer);
                exoPlayer.addListener(new OnTrackFinished());
                exoPlayer.sendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
                exoPlayer.setPlayWhenReady(true);
                /*

                MoviePlayer.PlayTask mPlayTask = new MoviePlayer.PlayTask(player, new MoviePlayer.PlayerFeedback() {
                    @Override
                    public void playbackStopped() {
                        if (position < files.size() && active) {
                            playVideo();
                        } else {

                            Intent returnIntent = new Intent();

                            returnIntent.putExtra("result", MainActivity.RESULT_FINISH_PLAY);

                            setResult(RESULT_OK, returnIntent);

                            VideoActivity.this.finish();
                        }
                    }
                });*/

                sendPlayCampaignFile();

                position++;


            } catch (Exception ex) {
                Log.e("VideoActivity", ex.getMessage(), ex);
                Intent intent = new Intent(MainActivity.MAKE_TOAST);
                intent.putExtra("Toast", ex.toString());
                bManager.sendBroadcast(intent);

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

                return true;
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        active = true;

        hideSystemUI();

        if (getIntent().getExtras().getInt("orientation") == MainActivity.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        if (position > files.size() - 1) {
            position = 0;
        }

        videoView = (TextureView) findViewById(R.id.videoview);

        videoView.setSurfaceTextureListener(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        cleanUp();
        active = false;

    }

    private void sendPlayCampaignFile() {
        Intent playFile = new Intent(MainActivity.CURRENT_FILE_ID);
        playFile.putExtra("fileId", files.get(position).getId());
        LocalBroadcastManager.getInstance(VideoActivity.this).sendBroadcast(playFile);
    }

    private void cleanUp() {

        if(exoPlayer != null){
            exoPlayer.release();
            exoPlayer = null;
//            audioRenderer = null;
//            videoRenderer = null;
        }
    }

    @Override
    protected void onDestroy() {
        cleanUp();

        bManager.unregisterReceiver(bReceiver);

        super.onDestroy();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        playVideo();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private class MediaCodecVideoTrackRendererEventListener implements MediaCodecVideoTrackRenderer.EventListener {
        @Override
        public void onDroppedFrames(int i, long l) {

        }

        @Override
        public void onVideoSizeChanged(int i, int i2, float v) {

        }

        @Override
        public void onDrawnToSurface(Surface surface) {

        }

        @Override
        public void onDecoderInitializationError(MediaCodecTrackRenderer.DecoderInitializationException e) {
            bManager.sendBroadcast(new ToastIntent("DecoderInitializationError"));
            cleanUp();

        }

        @Override
        public void onCryptoError(MediaCodec.CryptoException e) {

        }
    };

    private class OnTrackFinished implements ExoPlayer.Listener {

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

            if (playbackState == ExoPlayer.STATE_ENDED) {

                if (position == files.size()) {
                    cleanUp();

                    Intent returnIntent = new Intent();

                    returnIntent.putExtra("result", 1);

                    VideoActivity.this.setResult(RESULT_OK, returnIntent);

                    VideoActivity.this.finish();
                } else {
                    playVideo();
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
        private final String RECEIVER_STRING = VIDEO_ACTIVITY + "BroadcastReceiver";
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MainActivity.ACTIVITY_FINISH)) {
                Intent returnIntent = new Intent();

                returnIntent.putExtra("result", MainActivity.RESULT_FINISH_PLAY);

                VideoActivity.this.setResult(RESULT_OK, returnIntent);

                VideoActivity.this.finish();
            }  else if (action.equals(MainActivity.NO_NETWORK)){
                Log.d(RECEIVER_STRING, "NO NETWORK");
                try {
                    new NoNetworkDialog().show(getFragmentManager(),"NO_NETWORK");
                } catch (IllegalStateException ex){
            }
            }
        }
    };
}
