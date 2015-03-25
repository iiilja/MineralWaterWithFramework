package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import ee.promobox.promoboxandroid.data.CampaignFile;
import ee.promobox.promoboxandroid.data.CampaignFileType;
import ee.promobox.promoboxandroid.data.Display;
import ee.promobox.promoboxandroid.data.ErrorMessage;
import ee.promobox.promoboxandroid.interfaces.FragmentPlaybackListener;
import ee.promobox.promoboxandroid.interfaces.VideoWallMasterListener;
import ee.promobox.promoboxandroid.util.VideoMatrixCalculator;
import ee.promobox.promoboxandroid.util.geom.Rectangle;
import ee.promobox.promoboxandroid.util.geom.TriangleEquilateral;
import ee.promobox.promoboxandroid.widgets.FragmentVideoWall;
import ee.promobox.promoboxandroid.widgets.WallImageView;

/**
 * Created by ilja on 2.03.2015.
 */
public class FragmentWallVideo extends FragmentVideoWall implements TextureView.SurfaceTextureListener {


    private static final String TAG = "FragmentWallVideo";

    private View fragmentView;
    private TextureView videoView;
    private MoviePlayer player;

    private int viewOriginalHeight = 0;
    private int viewOriginalWidth = 0;


    private FragmentPlaybackListener playbackListener;
    private VideoWallMasterListener masterListener;

    private MainActivity mainActivity;

    private boolean textureAvailable;
    private boolean requestedStop = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        fragmentView = inflater.inflate(R.layout.fragment_wall_video, container, false);
        fragmentView.setOnLongClickListener(mainActivity);

        videoView = (TextureView) fragmentView.findViewById(R.id.video_texture_view);
        super.setView(fragmentView);
        return fragmentView;
    }

    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach");
        super.onAttach(activity);
        playbackListener = (FragmentPlaybackListener) activity;
        masterListener = (VideoWallMasterListener) activity;
        mainActivity = (MainActivity) activity;
    }

    @Override
    public void onCreate(Bundle b) {
        Log.d(TAG,"onCreate");
        super.onCreate(b);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
//        videoView.removeCallbacks(runnable);
        Display display = mainActivity.getDisplay();
        if (display != null) {
            Point[] points = display.getPoints();
//            slide.setInitialValues(mainActivity.getWallHeight(), mainActivity.getWallWidth(), points);
            float rotation = (float) - TriangleEquilateral.getAngleAlpha(points[3], points[0]);
            Log.d(TAG, "rotation = " + rotation );
//            videoView.setRotation(rotation);
        } else {
            Log.e(TAG, "NO DISPLAY CHOSEN");
            Point[] points = DEFAULT_POINTS;
//            slide.setInitialValues(DEFAULT_HEIGHT, DEFAULT_WIDTH, points);
//            slide.setRotation(0f);
        }

        if (textureAvailable){
            tryNextFile();
        }
        requestedStop = false;
        videoView.setSurfaceTextureListener(this);

    }

    @Override
    public void onClick(View v) {
        if (mainActivity.isMaster()) {
            super.onClick(v);
        }
    }

    private void tryNextFile() {
        CampaignFile campaignFile = mainActivity.getNextFile(CampaignFileType.VIDEO);

        if (campaignFile != null) {
            playVideo(campaignFile);
        } else {
            cleanUp();
            playbackListener.onPlaybackStop();
        }
    }


    public void playVideo(CampaignFile campaignFile) {
        try {
            cleanUp();
            String pathToFile = campaignFile.getPath();

            if (mainActivity.getOrientation() == MainActivity.ORIENTATION_PORTRAIT_EMULATION){
                Point videoSize = VideoMatrixCalculator.calculateVideoSize(pathToFile);
                videoSize = VideoMatrixCalculator.calculateNeededVideoSize(videoSize,viewOriginalHeight,viewOriginalWidth);
                RelativeLayout.LayoutParams relPar = (RelativeLayout.LayoutParams) videoView.getLayoutParams();
                relPar.width = videoSize.x;
                relPar.height = videoSize.y;
                videoView.setLayoutParams(relPar);

//                fragmentVideoLayout.setRotation(270);
            }

            Log.d(TAG,"playVideo() file = " + FilenameUtils.getBaseName(pathToFile));
            Log.d(TAG,pathToFile);

            Point [] points = mainActivity.getDisplay().getPoints();
            float rotation = (float) - TriangleEquilateral.getAngleAlpha(points[3], points[0]);

            Matrix matrix = new Matrix();
            RectF src = new RectF(0,0,mainActivity.getWallWidth(),mainActivity.getWallHeight());
            Rect rect = Rectangle.getOuterRect(points);
            RectF dst = new RectF(rect.left,rect.bottom,rect.right,rect.top);
            Log.w(TAG, "*** src");
            Log.d(TAG, "bottom = " + src.bottom + " top " + src.top);
            Log.d(TAG, "left = " + src.left + " right " + src.right);

            Log.w(TAG, "*** dst");
            Log.d(TAG, "bottom = " + dst.bottom + " top " + dst.top);
            Log.d(TAG, "left = " + dst.left + " right " + dst.right);

//            boolean isSet =  matrix.setRectToRect(dst, src, Matrix.ScaleToFit.CENTER );
//            Log.w(TAG, "matrix.setRectToRect is " + isSet);
//            matrix.setRotate(-45);
            Log.d(TAG, matrix.toString());
            videoView.setTransform(matrix);
//            videoView.setTranslationY(0);
//            videoView.setTranslationX(1084);
//            videoView.setRotation(0);
            Log.d(TAG, "height = " + videoView.getLayoutParams().height + " width = " + videoView.getLayoutParams().width);


            Surface surface = new Surface(videoView.getSurfaceTexture());
            try {
                player = new MoviePlayer(
                        new File(campaignFile.getPath()), surface, new SpeedControlCallback());
            } catch (IOException ioe) {
                Log.e("Vi", "Unable to play movie", ioe);
                surface.release();
                return;
            }

            MoviePlayer.PlayTask mPlayTask = new MoviePlayer.PlayTask(player, new MoviePlayer.PlayerFeedback() {
                @Override
                public void playbackStopped() {
                    Log.d(TAG, "playbackStopped  - requestedStop = " + requestedStop);
                    if ( !requestedStop){
                        tryNextFile();
                    }
                    requestedStop = false;
                }
            });

            mPlayTask.execute();


        } catch (Exception ex) {
            Log.e("VideoActivity", ex.getMessage(), ex);

            Intent returnIntent = new Intent();

            returnIntent.putExtra("result", MainActivity.RESULT_FINISH_PLAY);

        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        cleanUp();
        super.onPause();

    }

    public void cleanUp() {
        if (player !=null){
            requestedStop = true;
            player.requestStop();
        }
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        Log.d(TAG, "onSurfaceTextureAvailable");
        viewOriginalHeight =  videoView.getMeasuredHeight();
        viewOriginalWidth =  videoView.getMeasuredWidth();
        textureAvailable = true;
        tryNextFile();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d(TAG, "onSurfaceTextureDestroyed");
        textureAvailable = false;
        return true;
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

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onPlayerPause() {

    }

    @Override
    public void onPlayerPlay() {

    }

    @Override
    public void onPlayerPrevious() {

    }

    @Override
    public void onPlayerNext() {

    }
}
