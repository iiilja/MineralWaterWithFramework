package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ee.promobox.promoboxandroid.util.FragmentPlaybackListener;



public class FragmentMain extends Fragment {

    private FragmentPlaybackListener listener;
    private MainActivity mainActivity;
    private View fragment_main_layout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragment_main_layout = inflater.inflate(R.layout.fragment_main, container ,false);
//        if (view != null) {
//            ViewGroup parent = (ViewGroup) view.getParent();
//            if (parent != null) {
//                parent.removeView(view);
//            }
//        }
//        return view;
        return fragment_main_layout;
    }

    @Override
    public void onAttach(Activity activity) {
        mainActivity = (MainActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mainActivity.getOrientation() == MainActivity.ORIENTATION_PORTRAIT_EMULATION){
            fragment_main_layout.setRotation(270);
        }
    }
}
