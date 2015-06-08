package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.SeekBar;

import ee.promobox.promoboxandroid.data.CampaignFile;
import ee.promobox.promoboxandroid.data.CampaignFileType;
import ee.promobox.promoboxandroid.interfaces.FragmentPlaybackListener;
import ee.promobox.promoboxandroid.widgets.FragmentWithSeekBar;

/**
 * Created by ilja on 31.03.2015.
 */
public class FragmentWeb extends FragmentWithSeekBar implements View.OnTouchListener{
    private long touchDown;

    private static final String TAG = "FragmentWeb";

    private final String USER_AGENT = "Mozilla/5.0 (Linux; U; Android 4.1.1; en-gb; Build/KLP) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Safari/534.30";

    private FragmentPlaybackListener playbackListener;
    private MainActivity mainActivity;

    private WebView webView;
    private View fragmentView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_web,container,false);
        fragmentView.setOnLongClickListener(mainActivity);
        fragmentView.setOnClickListener(this);

        super.setView(fragmentView.findViewById(R.id.player_controls));

        webView = (WebView) fragmentView.findViewById(R.id.webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
//        settings.setUserAgentString(USER_AGENT);

        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setAllowFileAccess(false);

        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

        webView.setWebViewClient(new MyWebViewClient());
        webView.setOnTouchListener(this);
        return fragmentView;
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
        webView.removeCallbacks(runnable);

        if (mainActivity.getOrientation() == MainActivity.ORIENTATION_PORTRAIT_EMULATION) {
            fragmentView.setRotation(270);
        }
        tryNextFile();

    }

    private void tryNextFile(){
        CampaignFile campaignFile = mainActivity.getNextFile(CampaignFileType.HTML);
        if (campaignFile != null) {
            cleanUp();
            playWeb(campaignFile);
        } else {
            playbackListener.onPlaybackStop();
        }
    }

    private void playWeb(CampaignFile campaignFile) {
        Log.d(TAG,"playing " + campaignFile.getPath());
        setStatus(campaignFile.getName());
        int delay = campaignFile.getDelay() * 1000;
        super.setSeekBarMax(delay);
        super.changeSeekBarState(true, 0);
        webView.postDelayed(runnable, delay);
        webView.loadUrl(campaignFile.getName());

    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        webView.removeCallbacks(runnable);
        super.onPause();

    }



    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            tryNextFile();
        }
    };

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        webView.removeCallbacks(runnable);
        webView.postDelayed(runnable, seekBar.getMax() - seekBar.getProgress());
    }

    @Override
    public void onPlayerPause() {
        webView.removeCallbacks(runnable);
    }

    @Override
    public void onPlayerPlay() {
        webView.removeCallbacks(runnable);
        webView.postDelayed(runnable, getRemainingTime());
    }

    @Override
    public void onPlayerPrevious() {
        webView.removeCallbacks(runnable);
        mainActivity.setPreviousFilePosition();
        tryNextFile();
    }

    @Override
    public void onPlayerNext() {
        webView.removeCallbacks(runnable);
        tryNextFile();
    }

    @Override
    public void onSettingsPressed() {
        fragmentView.performLongClick();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.equals(webView)){
            fragmentView.removeCallbacks(longClickRunnable);
            touchDown = 0;
            if (event.getAction() == MotionEvent.ACTION_DOWN){
                touchDown = System.currentTimeMillis();
                fragmentView.postDelayed(longClickRunnable,1000);
            } else if (event.getAction() == MotionEvent.ACTION_UP){
                super.onClick(v);
            }
        }
        return v.onTouchEvent(event);
    }

    private class MyWebViewClient extends WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            webView.loadUrl(url);
            return true;
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

    }

    private Runnable longClickRunnable = new Runnable() {
        @Override
        public void run() {
            if (touchDown >= 1000){
                touchDown = 0;
                fragmentView.performLongClick();
            }
        }
    };
}
