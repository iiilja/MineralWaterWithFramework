package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Vector;

//http://thebitplague.wordpress.com/2013/04/05/kiosk-mode-on-the-nexus-7/
//http://www.tutorialspoint.com/android/android_imageswitcher.htm
public class MainActivity extends Activity {

    ImageView slide;

    Vector<Integer> imageIds;
    LinearLayout linLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        slide = (ImageView) findViewById(R.id.slide_1);

        slide.setImageResource(R.drawable.test);

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.test2);

        imageViewAnimatedChange(getBaseContext(), slide, bm);

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




}
