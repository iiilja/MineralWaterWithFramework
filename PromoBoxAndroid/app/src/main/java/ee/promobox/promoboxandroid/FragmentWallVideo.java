package ee.promobox.promoboxandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.TextureView;
import android.view.View;

import java.util.ArrayList;

import ee.promobox.promoboxandroid.data.CampaignFile;
import ee.promobox.promoboxandroid.widgets.FragmentVideoWall;

/**
 * Created by ilja on 2.03.2015.
 */
public class FragmentWallVideo extends FragmentVideoWall implements TextureView.SurfaceTextureListener {


    private TextureView videoView;
    private LocalBroadcastManager bManager;
    private ArrayList<CampaignFile> files;
    private int position = 0;
    private MoviePlayer player;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

//        setContentView(R.layout.activity_video);
//
//        bManager = LocalBroadcastManager.getInstance(this);
//
//        IntentFilter intentFilter = new IntentFilter();
//
//        intentFilter.addAction(MainActivity.ACTIVITY_FINISH);
//
//        bManager.registerReceiver(bReceiver, intentFilter);
//
//        Bundle extras = getIntent().getExtras();
//
//        files = extras.getParcelableArrayList("files");

    }


    public void playVideo() {/*
        if (files.size() > 0) {
            try {

                Surface surface = new Surface(videoView.getSurfaceTexture());

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
                        if (position < files.size()) {
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

                Intent returnIntent = new Intent();

                returnIntent.putExtra("result", MainActivity.RESULT_FINISH_PLAY);

                VideoActivity.this.setResult(RESULT_OK, returnIntent);

                VideoActivity.this.finish();
            }
        }*/
    }

    private void hideSystemUI() {/*

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
        });*/
    }


    @Override
    public void onResume() {/*
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

        videoView = (TextureView) findViewById(R.id.videoview);

        videoView.setSurfaceTextureListener(this);*/

    }

    @Override
    public void onPause() {
        super.onPause();
        cleanUp();

    }

    private void sendPlayCampaignFile() {/*
        Intent playFile = new Intent(MainActivity.CURRENT_FILE_ID);
        playFile.putExtra("fileId", files.get(position).getId());
        LocalBroadcastManager.getInstance(VideoActivity.this).sendBroadcast(playFile);*/
    }

    private void cleanUp() {
        player.requestStop();
    }

    @Override
    public void onDestroy() {
        cleanUp();

        bManager.unregisterReceiver(bReceiver);

        super.onDestroy();
    }


    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {/*
            if (intent.getAction().equals(MainActivity.ACTIVITY_FINISH)) {
                Intent returnIntent = new Intent();

                returnIntent.putExtra("result", MainActivity.RESULT_FINISH_PLAY);

                VideoActivity.this.setResult(RESULT_OK, returnIntent);

                VideoActivity.this.finish();
            }*/
        }
    };




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
    public void playFile(CampaignFile campaignFile, long frameId) {

    }

    @Override
    public void prepareFile(CampaignFile campaignFile) {

    }
}