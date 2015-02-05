package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
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
import ee.promobox.promoboxandroid.util.FragmentPlaybackListener;


public class FragmentVideo extends Fragment implements TextureView.SurfaceTextureListener,
        MediaCodecVideoTrackRenderer.EventListener , ExoPlayer.Listener{
    private final String TAG = "VideoActivity ";

    private TextureView videoView;


    private LocalBroadcastManager bManager;
    private ArrayList<CampaignFile> files;
    private int position = 0;
    private boolean active = true;
    private boolean silentMode = false;

    private ExoPlayer exoPlayer;
    private MediaCodecAudioTrackRenderer audioRenderer;
    private MediaCodecVideoTrackRenderer videoRenderer;

    private int viewOriginalHeight = 0;
    private int viewOriginalWidth = 0;
    private FragmentPlaybackListener playbackListener;
    private MainActivity mainActivity;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_video,container,false);
        videoView = (TextureView) view.findViewById(R.id.video_texture_view);
        return view;
    }


    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach");
        super.onAttach(activity);
        playbackListener = (FragmentPlaybackListener) activity;
        mainActivity = (MainActivity) activity;

    }

    public void playVideo() {
        if (files.size() > 0) {
            try {
                if (mainActivity.getOrientation() == MainActivity.ORIENTATION_PORTRAIT_EMULATION){
                    Point videoSize = calculateVideoSize();
                    if (position == 0){
                        viewOriginalHeight =  videoView.getMeasuredHeight();
                        viewOriginalWidth =  videoView.getMeasuredWidth();
                    }
                    videoSize = calculateNeededVideoSize(videoSize,viewOriginalWidth,viewOriginalHeight);
                    RelativeLayout.LayoutParams relPar = (RelativeLayout.LayoutParams) videoView.getLayoutParams();
                    relPar.width = videoSize.x;
                    relPar.height = videoSize.y;
                    videoView.setLayoutParams(relPar);
                }

                cleanUp();
                Surface surface = new Surface(videoView.getSurfaceTexture());
                String pathToFile = files.get(position).getPath();
                Log.d(TAG,"playVideo() file = " + FilenameUtils.getBaseName(pathToFile));
                Log.d(TAG,pathToFile);
                Uri uri = Uri.parse(pathToFile);
                SampleSource source = new FrameworkSampleSource(getActivity(), uri, null, 2);
                audioRenderer = new MediaCodecAudioTrackRenderer(
                        source, null, true);
                videoRenderer = new MediaCodecVideoTrackRenderer(source,
                        MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 0, new Handler(getActivity().getMainLooper()),
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
                makeToast(String.format(
                        MainActivity.ERROR_MESSAGE, 41, ex.getClass().getSimpleName()));
                bManager.sendBroadcast(new ErrorMessageIntent(ex));

                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", MainActivity.RESULT_FINISH_PLAY);
                playbackListener.onPlaybackStop();
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        active = true;
        silentMode = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("silent_mode", false);

        files = mainActivity.getFilePack() != null ? mainActivity.getFilePack() : new ArrayList<CampaignFile>();

        if (position > files.size() - 1) {
            position = 0;
        }

        if (mainActivity.getOrientation() == MainActivity.ORIENTATION_PORTRAIT_EMULATION){
            videoView.setRotation(270.0f);
        }

        videoView.setSurfaceTextureListener(this);

    }

    @Override
    public void onPause() {
        super.onPause();
        cleanUp();
        active = false;

    }

    private void sendPlayCampaignFile() {
        mainActivity.setCurrentFileId(files.get(position).getId());
        Log.d(TAG, files.get(position).getPath());
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
    public void onDestroy() {
        cleanUp();

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
        makeToast("Video player decoder initialization error");
        bManager.sendBroadcast(new ErrorMessageIntent(e));
        Log.e(TAG, "onDecoderInitializationError");
        cleanUp();

    }

    @Override
    public void onCryptoError(MediaCodec.CryptoException e) {
        makeToast("Video player crypto error");
        bManager.sendBroadcast(new ErrorMessageIntent(e));
        Log.e(TAG,"onCryptoError");
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
        Log.d(TAG,"STATE CHANGED , state = " + playbackState);

        if (playbackState == ExoPlayer.STATE_ENDED) {

            if (position == files.size()) {
                cleanUp();
                Log.d(TAG, "POSITION == files.size");

                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", 1);
                playbackListener.onPlaybackStop();
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
        makeToast("Player ERROR ");
        bManager.sendBroadcast(new ErrorMessageIntent(ex));
        Log.e(TAG, "onPlayerError");
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
            Log.d(TAG, height + "h , w = " + width);
            Point size = new Point();
            size.set(Integer.parseInt(width), Integer.parseInt(height));
            return size;

        } catch (NumberFormatException ex) {
            makeToast(String.format(
                    MainActivity.ERROR_MESSAGE, 42, ex.getClass().getSimpleName()));
            bManager.sendBroadcast(new ErrorMessageIntent(ex));
            Log.e(TAG, ex.getMessage());
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

    private void makeToast(String toast){
        if (!silentMode){
            Toast.makeText(getActivity(),toast ,Toast.LENGTH_LONG).show();
        }
    }
}
