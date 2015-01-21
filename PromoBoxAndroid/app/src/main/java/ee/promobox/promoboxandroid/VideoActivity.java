package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

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
    private int orientation;
    private boolean active = true;
    private MoviePlayer player;

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

                Surface surface = new Surface(videoView.getSurfaceTexture());

                if (orientation == MainActivity.ORIENTATION_PORTRAIT_EMULATION){
                    videoView.setRotation(270.0f);
                    Point videoSize = calculateVideoSize();
                    int height = videoView.getMeasuredHeight();
                    int width = videoView.getMeasuredWidth();
                    videoSize = calculateNeededVideoSize(videoSize,width,height);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(videoSize.x, videoSize.y);
                    params.gravity = Gravity.CENTER;
                    videoView.setLayoutParams(params);
                }


                try {
                    player = new MoviePlayer(
                            new File(files.get(position).getPath()), surface, new SpeedControlCallback());
                } catch (IOException ioe) {
                    Log.e("Vi", "Unable to play movie", ioe);
                    surface.release();
                    return;
                }

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
                });

                mPlayTask.execute();

                sendPlayCampaignFile();

                position++;


            } catch (Exception ex) {
                Log.e("VideoActivity", ex.getMessage(), ex);
                bManager.sendBroadcast(new ToastIntent(ex.getMessage()));

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
        if (player != null){
            player.requestStop();
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

        } catch (NumberFormatException e) {
            Log.e(VIDEO_ACTIVITY, e.getMessage());
        }
        return null;
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
