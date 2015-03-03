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

import ee.promobox.promoboxandroid.data.CampaignFile;
import ee.promobox.promoboxandroid.data.CampaignFileType;
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
    Point[] monitorPoints = new Point[4];

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

        monitorPoints[0] = new Point(1080,1920);        // 1
        monitorPoints[1] = new Point(1080,0);
        monitorPoints[2] = new Point(0,0);
        monitorPoints[3] = new Point(0,1920);
//
//        monitorPoints[0] = new Point(2160,1920);      // 2
//        monitorPoints[1] = new Point(2160,0);
//        monitorPoints[2] = new Point(1080,0);
//        monitorPoints[3] = new Point(1080,1920);

//        monitorPoints[0] = new Point(3240,1920);      // 3
//        monitorPoints[1] = new Point(3240,0);
//        monitorPoints[2] = new Point(2160,0);
//        monitorPoints[3] = new Point(2160,1920);
//
//        monitorPoints[0] = new Point(0,1500);        // 1
//        monitorPoints[1] = new Point(1920,1500);
//        monitorPoints[2] = new Point(1920,420);
//        monitorPoints[3] = new Point(0,420);

//        monitorPoints[0] = new Point(3000,1920);        // 2
//        monitorPoints[1] = new Point(3000,0);
//        monitorPoints[2] = new Point(1920,0);
//        monitorPoints[3] = new Point(1920,1920);

        slide.setInitialValues(1920,3240,monitorPoints);

        slide.setRotation((float) - TriangleEquilateral.getAngleAlpha(monitorPoints[3], monitorPoints[0]));

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
                playImage();
            }
        }

    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        slide.removeCallbacks(runnable);
        cleanUp();
        slide.setImageDrawable(null);
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

    private void cleanUp() {
        recycleBitmap();
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
        }
    }

    @Override
    public void prepareFile(CampaignFile campaignFile) {
        prepareNextFile(campaignFile);
    }

}
