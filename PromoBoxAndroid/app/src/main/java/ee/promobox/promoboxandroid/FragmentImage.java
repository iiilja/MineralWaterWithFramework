package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;


import java.io.File;
import java.util.ArrayList;

import ee.promobox.promoboxandroid.data.CampaignFile;
import ee.promobox.promoboxandroid.data.ErrorMessage;
import ee.promobox.promoboxandroid.util.FragmentPlaybackListener;

public class FragmentImage extends Fragment {

    private final String IMAGE_FRAGMENT_STRING = "ImageFragment ";

    private ImageView slide;
    private ArrayList<CampaignFile> files;
    private int position = 0;
    private boolean active = true;
    private long delay = 5000;

    private boolean silentMode = false;

    private FragmentPlaybackListener playbackListener;

    private MainActivity mainActivity;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(IMAGE_FRAGMENT_STRING, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(IMAGE_FRAGMENT_STRING, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_image,null, false);

        slide = (ImageView) view.findViewById(R.id.slide_1);
        return view;
    }

    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        Log.d(IMAGE_FRAGMENT_STRING, "onInflate");
        super.onInflate(activity, attrs, savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        Log.d(IMAGE_FRAGMENT_STRING, "onAttach");
        super.onAttach(activity);
        playbackListener = (FragmentPlaybackListener) activity;
        mainActivity = (MainActivity) activity;
    }

    @Override
    public void onResume() {
        Log.d(IMAGE_FRAGMENT_STRING, "onResume");
        super.onResume();

        files = mainActivity.getFilePack() != null ? mainActivity.getFilePack() : new ArrayList<CampaignFile>();
        silentMode = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("silent_mode", false);
        active = true;

        if (position > files.size() - 1) {
            position = 0;
        }

        if (files.size() > 0) {
            playImage(files.get(position).getPath());
        }

    }

    @Override
    public void onPause() {
        Log.d(IMAGE_FRAGMENT_STRING, "onPause");
        super.onPause();
        active = false;
    }


    @Override
    public void onDestroy() {
        Log.d(IMAGE_FRAGMENT_STRING, "onDestroy");
        recycleBitmap();

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
            Log.e(IMAGE_FRAGMENT_STRING, ex.getMessage(), ex);
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
            Log.e(IMAGE_FRAGMENT_STRING, ex.getMessage(), ex);
            makeToast("Error rotating image");
            mainActivity.addError(new ErrorMessage(ex), false);
            return source;
        }
    }

    final Runnable r = new Runnable() {
        @Override
        public void run() {
            if (position == files.size() || !active) {

                playbackListener.onPlaybackStop();

            } else {
                playImage(files.get(position).getPath());
            }
        }
    };

    private void sendPlayCampaignFile() {
        mainActivity.setCurrentFileId(files.get(position).getId());
        Log.d(IMAGE_FRAGMENT_STRING, files.get(position).getPath());
    }

    private void playImage(String path) {
        File file = new File(path);
        if (!file.exists()){
            String message = " No file in path " + path;
            Log.e(IMAGE_FRAGMENT_STRING, message);
            makeToast(message);
            mainActivity.addError(new ErrorMessage(
                    "FileNotFoundException",IMAGE_FRAGMENT_STRING + message,null), false);
            position ++ ;
            slide.postDelayed(r, delay);
            return;
        }
        try {
            Bitmap bitmap = decodeBitmap(file);
            if (mainActivity.getOrientation() == MainActivity.ORIENTATION_PORTRAIT_EMULATION) {
                bitmap = rotateBitmap(bitmap, 270);
            }
            recycleBitmap();
            slide.setImageBitmap(bitmap);

            sendPlayCampaignFile();

            position++;

            slide.postDelayed(r, delay);


        } catch (Exception ex) {
            Log.e(IMAGE_FRAGMENT_STRING, ex.getMessage(), ex);
            Log.e(IMAGE_FRAGMENT_STRING, "Path = " + path );

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
        if (!silentMode){
            Toast.makeText(getActivity(),toast ,Toast.LENGTH_LONG).show();
        }
    }


}
