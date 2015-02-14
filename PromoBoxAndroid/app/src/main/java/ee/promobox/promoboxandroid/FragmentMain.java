package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import ee.promobox.promoboxandroid.util.FragmentPlaybackListener;
import ee.promobox.promoboxandroid.util.StatusEnum;
import ee.promobox.promoboxandroid.widgets.DownloadingAnimationDrawable;
import ee.promobox.promoboxandroid.widgets.NotActiveAnimationDrawable;


public class FragmentMain extends Fragment {
    private static final String TAG = "FragmentMain";

    private MainActivity mainActivity;
    private View fragment_main_layout;

    private StatusEnum statusEnum = null;
    private String status = "";

    private DownloadingAnimationDrawable downloadingAnimation ;
    private NotActiveAnimationDrawable notActiveAnimationDrawable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG,"onCreateView");
        fragment_main_layout = inflater.inflate(R.layout.fragment_main, container ,false);
        updateStatus(statusEnum,status);
        return fragment_main_layout;
    }

    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG,"onAttach");
        mainActivity = (MainActivity) activity;

        notActiveAnimationDrawable = new NotActiveAnimationDrawable(mainActivity.getBaseContext());
        downloadingAnimation = new  DownloadingAnimationDrawable(mainActivity.getBaseContext());
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        if (mainActivity.getOrientation() == MainActivity.ORIENTATION_PORTRAIT_EMULATION){
            fragment_main_layout.setRotation(270);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG,"onPause");
    }

    @Override
    public void onDestroy() {
        recycleBitmap(downloadingAnimation);
        recycleBitmap(notActiveAnimationDrawable);
        super.onDestroy();
    }

    private void recycleBitmap(AnimationDrawable ad) {
        if (ad != null) {
            ad.stop();
            for (int i = 0; i < ad.getNumberOfFrames(); ++i){
                Drawable frame = ad.getFrame(i);
                if (frame instanceof BitmapDrawable) {
                    ((BitmapDrawable)frame).getBitmap().recycle();
                }
                frame.setCallback(null);
            }
            ad.setCallback(null);
        }
    }


    public void updateStatus( StatusEnum statusEnum, String status ){
        Log.d(TAG, "updateStatus : " + status + " Enum = " + (statusEnum != null ? statusEnum.toString(): "null"));
        TextView textView = (TextView) fragment_main_layout.findViewById(R.id.main_activity_status);
        ImageView imageView = (ImageView) fragment_main_layout.findViewById(R.id.main_activity_status_image);
        if (textView != null){
            textView.setText(status);
            this.status = status;
        }
        if (imageView != null) {
            if (statusEnum != null && !statusEnum.equals(this.statusEnum) || statusEnum != null) {
                this.statusEnum = statusEnum;
                switch (statusEnum){
                    case DOWNLOADED:
                        Log.d(TAG, "setting null animation");
                        imageView.setImageDrawable(null);
                        break;
                    case DOWNLOADING:
                        Log.d(TAG, "setting downloadingAnimation");
                        imageView.setImageDrawable(downloadingAnimation);
                        break;
                    case NO_ACTIVE_CAMPAIGN:
                        Log.d(TAG, "setting ZZZ animation");
                        imageView.setImageDrawable(notActiveAnimationDrawable);
                        break;
                    case NO_FILES:
                        break;
                }
            } else if (statusEnum == null){
                imageView.setImageDrawable(null);
            }
        }

    }
}
