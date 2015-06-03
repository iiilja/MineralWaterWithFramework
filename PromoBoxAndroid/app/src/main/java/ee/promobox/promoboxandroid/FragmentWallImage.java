package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import ee.promobox.promoboxandroid.data.CampaignFile;
import ee.promobox.promoboxandroid.data.CampaignFileType;
import ee.promobox.promoboxandroid.data.Display;
import ee.promobox.promoboxandroid.data.ErrorMessage;
import ee.promobox.promoboxandroid.interfaces.FragmentPlaybackListener;
import ee.promobox.promoboxandroid.interfaces.VideoWallMasterListener;
import ee.promobox.promoboxandroid.util.geom.TriangleEquilateral;
import ee.promobox.promoboxandroid.widgets.FragmentVideoWall;
import ee.promobox.promoboxandroid.widgets.WallImageView;

/**
 * Created by ilja on 27.02.2015.
 */
public class FragmentWallImage extends FragmentVideoWall {

    private static final String TAG = "FragmentWallImage ";

    private WallImageView slide;
    private View imageFragment;


    private FragmentPlaybackListener playbackListener;
    private VideoWallMasterListener masterListener;

    private MainActivity mainActivity;

    private Bitmap preparedBitmap;
    private CampaignFile preparedCampaignFile;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        imageFragment = inflater.inflate(R.layout.fragment_wall_image, container, false);
        imageFragment.setOnLongClickListener(mainActivity);

        slide = (WallImageView) imageFragment.findViewById(R.id.slide_1);
        super.setView(imageFragment.findViewById(R.id.player_controls));

        return imageFragment;
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
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        slide.removeCallbacks(runnable);
        Display display = mainActivity.getDisplay();
        if (display != null) {
            Point[] points = display.getPoints();
            int wallWidth;
            int wallHeight;
            try {
                wallHeight = mainActivity.getWallHeight();
                wallWidth = mainActivity.getWallWidth();
            } catch (RemoteException e) {
                Log.e(TAG,"Setting WIDTH AND HEIGHT DEFAULT " + e.getMessage());
                wallWidth = DEFAULT_WIDTH;
                wallHeight = DEFAULT_HEIGHT;
            }
            slide.setInitialValues(wallHeight, wallWidth, points);
            float rotation = (float) - TriangleEquilateral.getAngleAlpha(points[3], points[0]);
            Log.d(TAG, "rotation = " + rotation );
            slide.setRotation(rotation);
        } else {
            Log.e(TAG, "NO DISPLAY CHOSEN");
            Point[] points = DEFAULT_POINTS;
            slide.setInitialValues(DEFAULT_HEIGHT, DEFAULT_WIDTH, points);
            slide.setRotation(0f);
        }

        tryNextFile();

    }

    @Override
    public void onClick(View v) {
        if (mainActivity.isMaster()) {
            super.onClick(v);
        }
    }

    private void tryNextFile() {
        if ( mainActivity.isMaster() ){
            if (preparedBitmap == null){
                masterListener.onFileNotPrepared();
                slide.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (preparedBitmap != null && preparedCampaignFile != null){
                            playImage();
                        } else {
                            Log.e(TAG,"Strill not prepared");
                            slide.postDelayed(this,50);
                        }
                    }
                },50);
            } else {
                masterListener.onFileStartedPlaying(preparedCampaignFile.getId(),0);
            }
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        if (slide != null) {
            slide.removeCallbacks(runnable);
            cleanUp();
            slide.setImageDrawable(null);
        }
        super.onPause();
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        slide.destroyDrawingCache();
        slide = null;

        super.onDestroy();
    }

    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (preparedCampaignFile != null) {
                masterListener.onFileStartedPlaying(preparedCampaignFile.getId(),0);
            }
        }
    };

    private void prepareNextFile(CampaignFile campaignFile){
        Log.d(TAG,"prepareNextFile");
        if (campaignFile != null) {
            if (mainActivity.isMaster()){
                mainActivity.getNextFile(CampaignFileType.IMAGE);
            }
            prepareBitmap(campaignFile);
        } else {
            preparedBitmap.recycle();
            preparedBitmap = null;
            preparedCampaignFile = null;
        }
    }

    @Override
    public void cleanUp() {
        recycleBitmap();
        super.cleanUp();
    }

    private void prepareBitmap(CampaignFile campaignFile){
        Log.d(TAG,"prepareBitmap");
        if (slide != null){
            preparedBitmap = slide.getImageBitmap(campaignFile.getPath());
            preparedCampaignFile = campaignFile;
        } else {
            preparedBitmap = null;
            preparedCampaignFile = null;
            if (mainActivity.isMaster()){
                playbackListener.onPlaybackStop();
            }
        }

    }


    private void playImage() {
        try {

//            slide.setRotation(imageView.getWholeViewRotation());

            slide.setImageBitmap(preparedBitmap);
            if (preparedBitmap == null) {
                Log.e(TAG, "preparedBitmap == null");
            }
            if (mainActivity.isMaster()) {
                int delay = preparedCampaignFile.getDelay() * 1000;
                super.setSeekBarMax(delay);
                super.changeSeekBarState(true, 0);
                setStatus(preparedCampaignFile.getName());
                slide.postDelayed(runnable, delay);
                slide.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        masterListener.onFileNotPrepared();
                    }
                },250);
            }


        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);

            makeToast(String.format(
                    MainActivity.ERROR_MESSAGE, 22, ex.getClass().getSimpleName()));
            mainActivity.addError(new ErrorMessage(ex), false);

            if (mainActivity.isMaster()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playbackListener.onPlaybackStop();
                    }
                },1000);
            }
        }

    }


    private void recycleBitmap() {
        BitmapDrawable toRecycle = (BitmapDrawable) (slide != null ? slide.getDrawable(): null);

        if (toRecycle != null && toRecycle.getBitmap() != null) {
            toRecycle.getBitmap().recycle();
        }
    }

    private void makeToast(String toast){
        mainActivity.makeToast(toast);
    }


    @Override
    public void playFile(CampaignFile campaignFile, long frameId) {
        Log.d(TAG,"playFile camp.f.id = " + campaignFile.getId() + " prepared id = "
                + (preparedCampaignFile != null ? preparedCampaignFile.getId(): "-1"));
        if (preparedCampaignFile != null && preparedCampaignFile.getId() == campaignFile.getId()){
            Log.d(TAG, " playFile - playImage();");
            playImage();
        } else {
            Log.d(TAG," playFile - prepareBitmap(campaignFile);");
            prepareBitmap(campaignFile);
            playImage();
        }
    }

    @Override
    public void prepareFile(CampaignFile campaignFile) {
        if (campaignFile.getType() == CampaignFileType.IMAGE){
            prepareNextFile(campaignFile);
        } else {
            playbackListener.onPlaybackStop();
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        slide.removeCallbacks(runnable);
        slide.postDelayed(runnable, seekBar.getMax() - seekBar.getProgress());
    }

    @Override
    public void onPlayerPause() {
        slide.removeCallbacks(runnable);
    }

    @Override
    public void onPlayerPlay() {
        slide.removeCallbacks(runnable);
        slide.postDelayed(runnable, getRemainingTime());
    }

    @Override
    public void onPlayerPrevious() {
        slide.removeCallbacks(runnable);
        mainActivity.setPreviousFilePosition();
        tryNextFile();
    }

    @Override
    public void onPlayerNext() {
        slide.removeCallbacks(runnable);
        tryNextFile();
    }

    @Override
    public void onSettingsPressed() {
        imageFragment.performLongClick();
    }
}
