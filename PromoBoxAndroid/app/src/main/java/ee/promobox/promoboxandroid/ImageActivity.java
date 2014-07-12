package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class ImageActivity extends Activity {

    ImageView slide;
    int currentPlace = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);

        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(MainActivity.ACTIVITY_FINISH);

        bManager.registerReceiver(bReceiver, intentFilter);

        slide = (ImageView) findViewById(R.id.slide_1);

        Bundle extras = getIntent().getExtras();

        Bitmap bm = BitmapFactory.decodeFile(extras.getString("source"));

        slide.setImageBitmap(bm);

        final Runnable mUpdateResults = new Runnable() {
            @Override
            public void run() {
//                Bitmap bm = BitmapFactory.decodeResource(getResources(), images[currentPlace]);
//
//                imageViewAnimatedChange(getBaseContext(), slide, bm);
//
//                currentPlace++;

//                if (currentPlace == images.length) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", 1);
                setResult(RESULT_OK,returnIntent);

                ImageActivity.this.finish();
//                }

            }
        };

        final Handler mHandler = new Handler();

        int delay = 3000; // delay for 1 sec.

        int period = 3000; // repeat every 4 sec.

        Timer timer = new Timer();

        timer.schedule(new TimerTask() {

            public void run() {

                mHandler.post(mUpdateResults);
            }

        }, delay);

    }



    @Override
    protected void onDestroy()
    {
        Drawable toRecycle= slide.getDrawable();

        if (toRecycle != null) {
            ((BitmapDrawable)slide.getDrawable()).getBitmap().recycle();
        }


        super.onDestroy();
    }



    public static void imageViewAnimatedChange(Context c, final ImageView v, final Bitmap new_image) {

        final Animation anim_out = AnimationUtils.loadAnimation(c, android.R.anim.fade_out);
        final Animation anim_in  = AnimationUtils.loadAnimation(c, android.R.anim.fade_in);

        anim_out.setAnimationListener(new Animation.AnimationListener()
        {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation)
            {
                v.setImageBitmap(new_image);

                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationRepeat(Animation animation) {}
                    @Override public void onAnimationEnd(Animation animation) {}
                });

                v.startAnimation(anim_in);
            }
        });

        v.startAnimation(anim_out);
    }

    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(MainActivity.ACTIVITY_FINISH)) {
                ImageActivity.this.finish();
            }
        }
    };
}
