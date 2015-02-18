package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;


import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.FrameworkSampleSource;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.SampleSource;


import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.lang.ref.WeakReference;

import ee.promobox.promoboxandroid.data.CampaignFile;
import ee.promobox.promoboxandroid.data.CampaignFileType;
import ee.promobox.promoboxandroid.data.ErrorMessage;
import ee.promobox.promoboxandroid.util.FragmentPlaybackListener;
import ee.promobox.promoboxandroid.widgets.FragmentWithSeekBar;


public class FragmentVideo extends FragmentWithSeekBar implements TextureView.SurfaceTextureListener,
        MediaCodecVideoTrackRenderer.EventListener , ExoPlayer.Listener{
    private static final String TAG = "VideoActivity ";

    private View fragmentVideoLayout;
    private TextureView videoView;

    private ExoPlayer exoPlayer;
    private MediaCodecAudioTrackRenderer audioRenderer;
    private MediaCodecVideoTrackRenderer videoRenderer;

    private int viewOriginalHeight = 0;
    private int viewOriginalWidth = 0;
    private FragmentPlaybackListener playbackListener;
    private MainActivity mainActivity;

    private Handler videoLengthHandler = new Handler();
    private Runnable videoLengthStopper;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        fragmentVideoLayout =  inflater.inflate(R.layout.fragment_video,container,false);
        super.setView(fragmentVideoLayout);
        videoView = (TextureView) fragmentVideoLayout.findViewById(R.id.video_texture_view);
        return fragmentVideoLayout;
    }


    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach");
        super.onAttach(activity);
        playbackListener = (FragmentPlaybackListener) activity;
        mainActivity = (MainActivity) activity;

        videoLengthStopper = new VideoLengthWatcher(this,playbackListener);

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
            Surface surface = new Surface(videoView.getSurfaceTexture());
            String pathToFile = campaignFile.getPath();

            if (mainActivity.getOrientation() == MainActivity.ORIENTATION_PORTRAIT_EMULATION){
                Point videoSize = calculateVideoSize(pathToFile);
                videoSize = calculateNeededVideoSize(videoSize,viewOriginalHeight,viewOriginalWidth);
                RelativeLayout.LayoutParams relPar = (RelativeLayout.LayoutParams) videoView.getLayoutParams();
                relPar.width = videoSize.x;
                relPar.height = videoSize.y;
                videoView.setLayoutParams(relPar);
//                fragmentVideoLayout.setRotation(270);
            }

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

        } catch (Exception ex) {
            Log.e("VideoActivity", ex.getMessage(), ex);
            makeToast(String.format(
                    MainActivity.ERROR_MESSAGE, 41, ex.getClass().getSimpleName()));
            mainActivity.addError(new ErrorMessage(ex),false);

            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", MainActivity.RESULT_FINISH_PLAY);
            playbackListener.onPlaybackStop();
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        if (mainActivity.getOrientation() == MainActivity.ORIENTATION_PORTRAIT_EMULATION){
//            videoView.setRotation(270.0f);
            fragmentVideoLayout.setRotation(270);
        }

        videoView.setSurfaceTextureListener(this);

    }

    @Override
    public void onPause() {
        super.onPause();
        cleanUp();

    }

    protected void cleanUp() {
        super.cleanUp();
        if(exoPlayer != null){
            exoPlayer.release();
            exoPlayer = null;
            audioRenderer = null;
            videoRenderer = null;
        }
        videoLengthHandler.removeCallbacks(videoLengthStopper);
    }

    @Override
    public void onDestroy() {
        cleanUp();
        super.onDestroy();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        viewOriginalHeight =  videoView.getMeasuredHeight();
        viewOriginalWidth =  videoView.getMeasuredWidth();
        tryNextFile();
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
        mainActivity.addError(new ErrorMessage(e), false);
        Log.e(TAG, "onDecoderInitializationError");
        cleanUp();

    }

    @Override
    public void onCryptoError(MediaCodec.CryptoException e) {
        makeToast("Video player crypto error");
        mainActivity.addError(new ErrorMessage(e), false);
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

        if (playbackState == ExoPlayer.STATE_READY){
            long duration = exoPlayer.getDuration();
            long position = exoPlayer.getCurrentPosition();
            Log.d(TAG, "onPlayerStateChanged , exoPlayer.getDuration()" + duration);
            if (duration != ExoPlayer.UNKNOWN_TIME){
                super.setSeekBarMax(duration);
                if (playWhenReady) {
                    super.changeSeekBarState(playWhenReady,(int) exoPlayer.getCurrentPosition());
                    videoLengthHandler.postDelayed(videoLengthStopper, duration - position + 10 * 1000);
                }
            }
        }

        if (playbackState == ExoPlayer.STATE_ENDED) {
            tryNextFile();
        }
    }

    @Override
    public void onPlayWhenReadyCommitted() {
    }

    @Override
    public void onPlayerError(ExoPlaybackException ex) {
        makeToast("Player ERROR ");
        mainActivity.addError(new ErrorMessage(ex), false);
        Log.e(TAG, "onPlayerError");
    }




    private Point calculateVideoSize(String pathToFile) {
        try {
            File file = new File(pathToFile);
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
            mainActivity.addError(new ErrorMessage(ex), false);
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
        mainActivity.makeToast(toast);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        long progress = seekBar.getProgress();
        if (exoPlayer != null){
            onPlayerPause();
            exoPlayer.seekTo(progress);
            onPlayerPlay();

            videoLengthHandler.removeCallbacks(videoLengthStopper);
            videoLengthHandler.postDelayed(videoLengthStopper, seekBar.getMax()-progress + 10 * 1000);
        }
    }

    @Override
    public void onPlayerPause() {
        Log.d(TAG, "onPlayerPause");
        videoLengthHandler.removeCallbacks(videoLengthStopper);
        exoPlayer.setPlayWhenReady(false);
    }

    @Override
    public void onPlayerPlay() {
        Log.d(TAG, "onPlayerPlay");

        videoLengthHandler.postDelayed(videoLengthStopper, exoPlayer.getDuration()-exoPlayer.getCurrentPosition() + 3 * 1000);
        exoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void onPlayerPrevious() {
        mainActivity.setPreviousFilePosition();
        tryNextFile();
    }

    @Override
    public void onPlayerNext() {
        tryNextFile();
    }


    private static final class VideoLengthWatcher implements Runnable {
        private final WeakReference<FragmentVideo> fragmentReference;
        private final WeakReference<FragmentPlaybackListener> playbackListenerReference;

        VideoLengthWatcher( FragmentVideo fragment, FragmentPlaybackListener playbackListener){
            fragmentReference = new WeakReference<>(fragment);
            playbackListenerReference = new WeakReference<>(playbackListener);
        }

        @Override
        public void run() {
            Log.e(FragmentVideo.TAG,"Executing runnable, smth wrong with player");
            FragmentVideo fragment = fragmentReference.get();
            FragmentPlaybackListener playbackListener = playbackListenerReference.get();
            if (fragment != null && playbackListener != null){
                fragment.cleanUp();
                playbackListener.onPlaybackStop();
            }
        }
    }
}
