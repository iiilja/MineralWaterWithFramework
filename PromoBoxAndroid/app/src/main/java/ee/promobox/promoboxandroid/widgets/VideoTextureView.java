package ee.promobox.promoboxandroid.widgets;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

/**
 * Created by ilja on 19.01.2015.
 */
public class VideoTextureView extends TextureView {
    public VideoTextureView(Context context) {
        super(context);
    }

    public VideoTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        if (widthMeasureSpec == 0 || heightMeasureSpec == 0) {
//            Log.w("AspectRatioImageView", "(widthMeasureSpec == 0 || heightMeasureSpec == 0");
//            return;
//        }
//        try {
//            Drawable drawable = getBackground();
//
//            if (drawable == null) {
//                setMeasuredDimension(0, 0);
//            } else {
//                float imageSideRatio = (float)drawable.getIntrinsicWidth() / (float)drawable.getIntrinsicHeight();
//                float viewSideRatio = (float)MeasureSpec.getSize(widthMeasureSpec) / (float)MeasureSpec.getSize(heightMeasureSpec);
//                if (imageSideRatio <= viewSideRatio) {
//                    // Image is taller than the display (ratio)
//                    int width = MeasureSpec.getSize(widthMeasureSpec);
//                    int height = (int)(width / imageSideRatio);
//                    setMeasuredDimension(width, height);
//                } else {
//                    // Image is wider than the display (ratio)
//                    int height = MeasureSpec.getSize(heightMeasureSpec);
//                    int width = (int)(height * imageSideRatio);
//                    setMeasuredDimension(width, height);
//                }
//            }
//        } catch (Exception e) {
//            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        }
//    }
}
