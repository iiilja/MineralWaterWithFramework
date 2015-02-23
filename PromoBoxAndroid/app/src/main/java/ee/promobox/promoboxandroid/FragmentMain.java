package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ee.promobox.promoboxandroid.util.StatusEnum;
import ee.promobox.promoboxandroid.widgets.MyAnimatedDrawable;


public class FragmentMain extends Fragment {
    private static final String TAG = "FragmentMain";

    private MainActivity mainActivity;
    private View fragment_main_layout;

    private StatusEnum statusEnum = null;
    private String status = "";

    private MyAnimatedDrawable downloadingAnimation ;
    private MyAnimatedDrawable notActiveAnimationDrawable;
    private AnimationDrawable previousAnimationDrawable;

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

        notActiveAnimationDrawable = new MyAnimatedDrawable(mainActivity.getBaseContext(),MyAnimatedDrawable.ZZZ, 0,49);
        downloadingAnimation = new MyAnimatedDrawable(mainActivity.getBaseContext(),MyAnimatedDrawable.DOWNLOADING,3,51);
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
        downloadingAnimation.recycleSelf();
        notActiveAnimationDrawable.recycleSelf();
        super.onDestroy();
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
            AnimationDrawable newAnimationDrawable = null;
            if (statusEnum != null && !statusEnum.equals(this.statusEnum) || statusEnum != null) {
                this.statusEnum = statusEnum;
                switch (statusEnum){
                    case DOWNLOADED:
                        Log.d(TAG, "setting null animation");
                        break;
                    case DOWNLOADING:
                        Log.d(TAG, "setting downloadingAnimation");
                        newAnimationDrawable = downloadingAnimation;
                        break;
                    case NO_ACTIVE_CAMPAIGN:
                        Log.d(TAG, "setting ZZZ animation");
                        newAnimationDrawable = notActiveAnimationDrawable;
                        break;
                    case NO_FILES:
                        Log.d(TAG, "setting ZZZ animation");
                        newAnimationDrawable = notActiveAnimationDrawable;
                        break;
                }
            } else {
                Log.d(TAG, "setting null animation");
            }
            imageView.setImageDrawable(newAnimationDrawable);
            if (previousAnimationDrawable != null && !previousAnimationDrawable.equals(newAnimationDrawable)){
                previousAnimationDrawable.stop();
            }
            if (newAnimationDrawable != null  && !newAnimationDrawable.equals(previousAnimationDrawable)) {
                newAnimationDrawable.start();
            }
            previousAnimationDrawable = newAnimationDrawable;
        }

    }
}
