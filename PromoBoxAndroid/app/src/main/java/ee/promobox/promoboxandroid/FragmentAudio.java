package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.FrameworkSampleSource;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.SampleSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import ee.promobox.promoboxandroid.data.CampaignFile;
import ee.promobox.promoboxandroid.data.ErrorMessage;
import ee.promobox.promoboxandroid.util.FragmentPlaybackListener;


//https://github.com/felixpalmer/android-visualizer
public class FragmentAudio extends Fragment implements ExoPlayer.Listener {

    private final String TAG = "AudioActivity ";

    ExoPlayer exoPlayer;
    MediaCodecAudioTrackRenderer audioRenderer;

    private FragmentPlaybackListener playbackListener;
    private MainActivity mainActivity;

    private View audioView;


    private ArrayList<CampaignFile> files;
    private int position = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        view.setOnLongClickListener(mainActivity);
        audioView = view;
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach");
        playbackListener = (FragmentPlaybackListener) activity;

        mainActivity = (MainActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onResume();

        files = mainActivity.getFilePack() != null ? mainActivity.getFilePack() : new ArrayList<CampaignFile>();

        if ( mainActivity.getOrientation() == MainActivity.ORIENTATION_PORTRAIT_EMULATION){
            audioView.setRotation(270);
        }

        if (position > files.size() - 1) {
            position = 0;
        }

        if (files.size() > 0) {
            try {
                playAudio();
            } catch (Exception ex) {
                Log.e(TAG, "onResume " + ex.getMessage());
                mainActivity.makeToast(String.format(
                        MainActivity.ERROR_MESSAGE, 11, ex.getClass().getSimpleName()));
                mainActivity.addError(new ErrorMessage(ex), false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tryNextFile();
                    }
                }, 1000);
            }
        }

    }


    @Override
    public void onDestroy() {
        cleanUp();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        cleanUp();
    }

    private void sendPlayCampaignFile() {
        mainActivity.setCurrentFileId(files.get(position).getId());
        Log.d(TAG, files.get(position).getPath());
    }

    private void playAudio() throws Exception{
        cleanUp();
        String pathToFile = files.get(position).getPath();
        File file = new File(pathToFile);
        if (!file.exists()){
            Log.e(TAG, "File not found : " + pathToFile);
            throw new FileNotFoundException("File not found : " + pathToFile);
        }
        Log.d(TAG,"playAudio() file = " + file.getName() + " PATH = " + pathToFile);
        setStatus(files.get(position).getName());
        Uri uri = Uri.parse(pathToFile);
        SampleSource source = new FrameworkSampleSource(getActivity(), uri, null, 1);
        audioRenderer = new MediaCodecAudioTrackRenderer(source);
        exoPlayer = ExoPlayer.Factory.newInstance(1);
        exoPlayer.prepare(audioRenderer);
        exoPlayer.setPlayWhenReady(true);
        sendPlayCampaignFile();
        exoPlayer.addListener(this);
    }

    private void cleanUp() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        audioRenderer = null;

    }


    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        if (playbackState == ExoPlayer.STATE_ENDED) {

            if (position + 1 == files.size()) {
                finishActivity();
            }
            else {
                tryNextFile();
            }
        }
    }

    @Override
    public void onPlayWhenReadyCommitted() {

    }

    @Override
    public void onPlayerError(ExoPlaybackException ex) {
        mainActivity.makeToast("Audio player error");
        mainActivity.addError(new ErrorMessage(ex), false);
        Log.e(TAG, "onPlayerError " + ex.getMessage());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                tryNextFile();
            }
        }, 1000);
    }

    private void finishActivity (){
        cleanUp();
        playbackListener.onPlaybackStop();
    }

    private void tryNextFile(){
        try {
            position ++;
            if (position < files.size()){
                playAudio();
            } else {
                mainActivity.makeToast("Audio player error");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finishActivity();
                    }
                }, 1000);
            }
        }
        catch (Exception ex){
            Log.e(TAG, "onPlayerError " + ex.getMessage());
            mainActivity.makeToast(String.format(
                    MainActivity.ERROR_MESSAGE, 12, ex.getClass().getSimpleName()));
            mainActivity.addError(new ErrorMessage(ex), false);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    tryNextFile();
                }
            }, 1000);
        }
    }

    private void setStatus(String status){
        TextView textView = (TextView)getActivity().findViewById(R.id.main_activity_status);
        textView.setText(status);
    }
}
