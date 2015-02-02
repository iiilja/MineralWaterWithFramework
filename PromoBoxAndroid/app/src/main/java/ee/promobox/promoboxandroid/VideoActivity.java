package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;


import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.FrameworkSampleSource;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.SampleSource;


import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;

import ee.promobox.promoboxandroid.data.CampaignFile;
import ee.promobox.promoboxandroid.intents.ErrorMessageIntent;
import ee.promobox.promoboxandroid.intents.ToastIntent;


public class VideoActivity extends Activity implements TextureView.SurfaceTextureListener,
        MediaCodecVideoTrackRenderer.EventListener , ExoPlayer.Listener{
    private final String VIDEO_ACTIVITY = "VideoActivity ";

    private TextureView videoView;


    private LocalBroadcastManager bManager;
    private ArrayList<CampaignFile> files;
    private int position = 0;
    private int orientation;
    private boolean active = true;

    private ExoPlayer exoPlayer;
    private MediaCodecAudioTrackRenderer audioRenderer;
    private MediaCodecVideoTrackRenderer videoRenderer;

    private int viewOriginalHeight = 0;
    private int viewOriginalWidth = 0;


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
                if (orientation == MainActivity.ORIENTATION_PORTRAIT_EMULATION){
                    Point videoSize = calculateVideoSize();
                    if (position == 0){
                        viewOriginalHeight =  videoView.getMeasuredHeight();
                        viewOriginalWidth =  videoView.getMeasuredWidth();
                    }
                    videoSize = calculateNeededVideoSize(videoSize,viewOriginalWidth,viewOriginalHeight);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(videoSize.x, videoSize.y);
                    params.gravity = Gravity.CENTER;
                    videoView.setLayoutParams(params);
                }

                cleanUp();
                Surface surface = new Surface(videoView.getSurfaceTexture());
                String pathToFile = files.get(position).getPath();
                Log.d(VIDEO_ACTIVITY,"playVideo() file = " + FilenameUtils.getBaseName(pathToFile));
                Log.d(VIDEO_ACTIVITY,pathToFile);
                Uri uri = Uri.parse(pathToFile);
                SampleSource source = new FrameworkSampleSource(this, uri, null, 2);
                audioRenderer = new MediaCodecAudioTrackRenderer(
                        source, null, true);
                videoRenderer = new MediaCodecVideoTrackRenderer(source,
                        MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 0, new Handler(getMainLooper()),
                        this, 50);
                exoPlayer = ExoPlayer.Factory.newInstance(2);
                exoPlayer.prepare(audioRenderer,videoRenderer);
                exoPlayer.addListener(this);
                exoPlayer.sendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
                exoPlayer.setPlayWhenReady(true);


                sendPlayCampaignFile();

                position++;


            } catch (Exception ex) {
                Log.e("VideoActivity", ex.getMessage(), ex);
                Toast.makeText(this, String.format(
                        MainActivity.ERROR_MESSAGE, 41, ex.getClass().getSimpleName()),
                        Toast.LENGTH_LONG).show();
                bManager.sendBroadcast(new ErrorMessageIntent(ex));

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

        orientation = getIntent().getExtras().getInt("orientation");

        if (orientation == MainActivity.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        if (position > files.size() - 1) {
            position = 0;
        }

        videoView = (TextureView) findViewById(R.id.videoview);

        if (orientation == MainActivity.ORIENTATION_PORTRAIT_EMULATION){
            videoView.setRotation(270.0f);
        }

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
            audioRenderer = null;
            videoRenderer = null;
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

    @Override
    public void onDecoderInitializationError(MediaCodecTrackRenderer.DecoderInitializationException e) {
        Toast.makeText(this, "Video player decoder initialization error", Toast.LENGTH_LONG).show();
        bManager.sendBroadcast(new ErrorMessageIntent(e));
        Log.e(VIDEO_ACTIVITY, "onDecoderInitializationError");
        cleanUp();

    }

    @Override
    public void onCryptoError(MediaCodec.CryptoException e) {
        Toast.makeText(this,"Video player crypto error", Toast.LENGTH_LONG).show();
        bManager.sendBroadcast(new ErrorMessageIntent(e));
        Log.e(VIDEO_ACTIVITY,"onCryptoError");
    }

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
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        Log.d(VIDEO_ACTIVITY,"STATE CHANGED , state = " + playbackState);

        if (playbackState == ExoPlayer.STATE_ENDED) {

            if (position == files.size()) {
                cleanUp();
                Log.d(VIDEO_ACTIVITY, "POSITION == files.size");

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
    public void onPlayerError(ExoPlaybackException ex) {
        Toast.makeText(this, "Player ERROR ",Toast.LENGTH_LONG).show();
        bManager.sendBroadcast(new ErrorMessageIntent(ex));
        Log.e(VIDEO_ACTIVITY, "onPlayerError");
    }




    private Point calculateVideoSize() {
        try {
            File file = new File(files.get(position).getPath());
            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
            metaRetriever.setDataSource(file.getAbsolutePath());
            String height = metaRetriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            String width = metaRetriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            Log.d(VIDEO_ACTIVITY, height + "h , w = " + width);
            Point size = new Point();
            size.set(Integer.parseInt(width), Integer.parseInt(height));
            return size;

        } catch (NumberFormatException ex) {
            Toast.makeText(this, String.format(
                    MainActivity.ERROR_MESSAGE, 42, ex.getClass().getSimpleName()),
                    Toast.LENGTH_LONG).show();
            bManager.sendBroadcast(new ErrorMessageIntent(ex));
            Log.e(VIDEO_ACTIVITY, ex.getMessage());
        }
        return new Point(0,0);
    }

    private Point calculateNeededVideoSize(Point videoSize, int viewHeight, int viewWidth) {
        int videoHeight = videoSize.y;
        int videoWidth = videoSize.x;
        float imageSideRatio = (float)videoWidth / (float)videoHeight;
        float viewSideRatio = (float) viewWidth / (float) viewHeight;
        if (imageSideRatio > viewSideRatio) {
            // Image is taller than the display (ratio)
            int height = (int)(viewWidth / imageSideRatio);
            videoSize.set(viewWidth, height);
        } else {
            // Image is wider than the display (ratio)
            int width = (int)(viewHeight * imageSideRatio);
            videoSize.set(width, viewHeight);
        }
        return videoSize;
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
            }
        }
    };
}
