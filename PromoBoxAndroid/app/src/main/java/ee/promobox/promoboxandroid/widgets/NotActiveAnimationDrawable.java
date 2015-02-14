package ee.promobox.promoboxandroid.widgets;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.io.IOException;

/**
 * Created by ilja on 13.02.2015.
 */
public class NotActiveAnimationDrawable extends AnimationDrawable {

    public NotActiveAnimationDrawable(Context context) {
        setAnimation(context);
    }

    private void setAnimation(Context context){
        Drawable d = null;
        String pattern = "zzz/zzz_000%d.png";
        try {
            AssetManager assets = context.getAssets();

            for (int i = 0; i < 50; i++) {
                BitmapFactory.Options options = new BitmapFactory.Options();

                options.inPurgeable = true;
                options.inInputShareable = true;
                options.inDither = false;
                options.inTempStorage = new byte[32 * 1024];
                Bitmap b = BitmapFactory.decodeStream(assets.open(String.format(pattern,i)),null, options);
                d = new BitmapDrawable(Resources.getSystem(),b);
//                d = Drawable.createFromStream(assets.open(String.format(pattern,i)), null);
                this.addFrame(d, 50);
            }

            this.setOneShot(false);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
