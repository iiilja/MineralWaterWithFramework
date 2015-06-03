package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;

import java.io.File;

import ee.promobox.promoboxandroid.data.CampaignFile;
import ee.promobox.promoboxandroid.data.CampaignFileType;
import ee.promobox.promoboxandroid.data.ErrorMessage;
import ee.promobox.promoboxandroid.interfaces.FragmentPlaybackListener;
import ee.promobox.promoboxandroid.widgets.FragmentWithSeekBar;

public class FragmentImage extends FragmentWithSeekBar {

    private static final String TAG = "ImageFragment ";

    private ImageView slide;
    private View imageFragment;


    private FragmentPlaybackListener playbackListener;

    private MainActivity mainActivity;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        imageFragment = inflater.inflate(R.layout.fragment_image, container, false);
        imageFragment.setOnLongClickListener(mainActivity);
        super.setView(imageFragment.findViewById(R.id.player_controls));

        slide = (ImageView) imageFragment.findViewById(R.id.slide_1);

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
        mainActivity = (MainActivity) activity;
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        slide.removeCallbacks(runnable);

        if (mainActivity.getOrientation() == MainActivity.ORIENTATION_PORTRAIT_EMULATION) {
            imageFragment.setRotation(270);
        }
        tryNextFile();

    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        slide.removeCallbacks(runnable);
        recycleBitmap();
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


    private Bitmap decodeBitmap(File file) {
        Bitmap bm = null;

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();

            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inDither = false;
            options.inTempStorage = new byte[32 * 1024];

            bm = BitmapFactory.decodeFile(file.getPath(), options);


        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);
            makeToast(String.format(
                    MainActivity.ERROR_MESSAGE, 21, ex.getClass().getSimpleName()));
            mainActivity.addError(new ErrorMessage(ex), false);
        }

        return bm;
    }

    public Bitmap rotateBitmap(Bitmap source, float angle) {
        try {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            Bitmap newBitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
            source.recycle();
            return newBitmap;
        } catch (Exception ex){
            Log.e(TAG, ex.getMessage(), ex);
            makeToast("Error rotating image");
            mainActivity.addError(new ErrorMessage(ex), false);
            return source;
        }
    }

    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            tryNextFile();
        }
    };

    private void tryNextFile(){
        CampaignFile campaignFile = mainActivity.getNextFile(CampaignFileType.IMAGE);
        if (campaignFile != null) {
            cleanUp();
            playImage(campaignFile);
        } else {
            playbackListener.onPlaybackStop();
        }
    }


    private void playImage(CampaignFile campaignFile) {
        String path = campaignFile.getPath();
        File file = new File(path);
        if (!file.exists()){
            String message = " No file in path " + path;
            Log.e(TAG, message);
            makeToast(message);
            mainActivity.addError(new ErrorMessage(
                    "FileNotFoundException", TAG + message,null), false);
            slide.postDelayed(runnable, 1000);
            return;
        }
        try {
            Bitmap bitmap = decodeBitmap(file);
            recycleBitmap();
//            imageView.setImageDrawable(file.getPath(),1920,3240,p);
            slide.setImageBitmap(bitmap);
            setStatus(campaignFile.getName());
            int delay = campaignFile.getDelay() * 1000;
            super.setSeekBarMax(delay);
            super.changeSeekBarState(true, 0);
            slide.postDelayed(runnable, delay);


        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);
            Log.e(TAG, "Path = " + path );

            makeToast(String.format(
                    MainActivity.ERROR_MESSAGE, 22, ex.getClass().getSimpleName()));
            mainActivity.addError(new ErrorMessage(ex), false);

            playbackListener.onPlaybackStop();
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
