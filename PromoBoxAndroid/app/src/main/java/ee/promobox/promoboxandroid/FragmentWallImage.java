package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
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

    private static final int DEFAULT_WIDTH = 1920;
    private static final int DEFAULT_HEIGHT = 1080;
    private static final Point[] DEFAULT_POINTS = {
            new Point(0,1080),
            new Point(1920,1080),
            new Point(1920,0),
            new Point(0,0)
    };

    private static final String TAG = "FragmentWallImage ";

    private WallImageView slide;
    private View imageFragment;


    private FragmentPlaybackListener playbackListener;
    private VideoWallMasterListener masterListener;

    private MainActivity mainActivity;
    private Point[] monitorPoints = new Point[4];

    private Bitmap preparedBitmap;
    private CampaignFile preparedCampaignFile;

    private boolean amMaster;


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
        super.setView(imageFragment);

        return imageFragment;
    }

    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        Log.d(TAG, "onInflate");
        super.onInflate(activity, attrs, savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach");
        super.onAttach(activity);
        playbackListener = (FragmentPlaybackListener) activity;
        masterListener = (VideoWallMasterListener) activity;
        mainActivity = (MainActivity) activity;
        amMaster = mainActivity.isMaster();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        slide.removeCallbacks(runnable);
        Display display = mainActivity.getDisplay();
        if (display != null) {
            Point[] points = display.getPoints();
            slide.setInitialValues(mainActivity.getWallHeight(), mainActivity.getWallWidth(), points);
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
        if (amMaster) {
            super.onClick(v);
        }
    }

    private void tryNextFile() {
        if ( amMaster ){
            if (preparedBitmap == null){
                masterListener.onFileNotPrepared();
                slide.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playImage();
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
            if (amMaster){
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
            if (amMaster){
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
            if (amMaster) {
                int delay = getArguments().getInt("delay");
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

            if (amMaster) {
                playbackListener.onPlaybackStop();
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
        prepareNextFile(campaignFile);
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
}
