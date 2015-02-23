package ee.promobox.promoboxandroid.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.IOException;

import ee.promobox.promoboxandroid.util.geom.Rectangle;

/**
 * Created by ilja on 19.02.2015.
 */
public class VideoWallImageView extends ImageView{


    private static final String TAG = "VideoWallImageView";
    // clockwise
    private final int UP_LEFT_CORNER = 0;
    private final int UP_CENTER = 1;
    private final int UP_RIGHT_CORNER = 2;
    private final int RIGHT_CENTER = 3;
    private final int DOWN_RIGHT_CORNER = 4;
    private final int DOWN_CENTER = 5;
    private final int DOWN_LEFT_CORNER = 6;
    private final int LEFT_CENTER = 7;



//    private static final int[] gravitiesRelative = {
//            RelativeLayout.ALIGN_PARENT_TOP | RelativeLayout.ALIGN_PARENT_LEFT | RelativeLayout.ALIGN_PARENT_START,
//            RelativeLayout.ALIGN_PARENT_TOP | RelativeLayout.CENTER_HORIZONTAL,
//            RelativeLayout.ALIGN_PARENT_TOP | RelativeLayout.ALIGN_PARENT_RIGHT | RelativeLayout.ALIGN_PARENT_END,
//            RelativeLayout.CENTER_VERTICAL | RelativeLayout.ALIGN_PARENT_RIGHT | RelativeLayout.ALIGN_PARENT_END,
//            RelativeLayout.ALIGN_PARENT_BOTTOM | RelativeLayout.ALIGN_PARENT_RIGHT | RelativeLayout.ALIGN_PARENT_END,
//            RelativeLayout.ALIGN_PARENT_BOTTOM | RelativeLayout.CENTER_HORIZONTAL,
//            RelativeLayout.ALIGN_PARENT_BOTTOM | RelativeLayout.ALIGN_PARENT_LEFT | RelativeLayout.ALIGN_PARENT_START,
//            RelativeLayout.CENTER_VERTICAL | RelativeLayout.ALIGN_PARENT_LEFT | RelativeLayout.ALIGN_PARENT_START,
//    };
//
//    private int[] gravities = {
//            Gravity.TOP | Gravity.LEFT,
//            Gravity.TOP | Gravity.CENTER_HORIZONTAL,
//            Gravity.TOP | Gravity.RIGHT,
//            Gravity.RIGHT | Gravity.CENTER_VERTICAL,
//            Gravity.BOTTOM | Gravity.RIGHT,
//            Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL,
//            Gravity.BOTTOM | Gravity.LEFT,
//            Gravity.LEFT | Gravity.CENTER_VERTICAL
//    };
//
//    private int[] gravitiesRotated = {
//            Gravity.BOTTOM | Gravity.LEFT,
//            Gravity.LEFT | Gravity.CENTER_VERTICAL,
//            Gravity.TOP | Gravity.LEFT,
//            Gravity.TOP | Gravity.CENTER_HORIZONTAL,
//            Gravity.TOP | Gravity.RIGHT,
//            Gravity.RIGHT | Gravity.CENTER_VERTICAL,
//            Gravity.BOTTOM | Gravity.RIGHT,
//            Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL
//    };



    private double ratio;

    public VideoWallImageView(Context context) {
        super(context);
    }

    public VideoWallImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoWallImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public VideoWallImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            int width = (int) (getDrawable().getIntrinsicWidth() / ratio);
            int height = (int) (getDrawable().getIntrinsicHeight() / ratio);
            Log.d(TAG, " setMeasuredDimension(width="+ width + ", height=" + height +") " );
            setMeasuredDimension(width, height);
        } catch (NullPointerException e) {
            super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        }
    }

    public void setImageDrawable(String filePath, int wallHeight, int wallWidth, Point[] monitorPoints) {
        monitorPoints[0] = new Point(2160,1920);
        monitorPoints[1] = new Point(2160,0);
        monitorPoints[2] = new Point(1080,0);
        monitorPoints[3] = new Point(1080,1920);

        Point topLeft = monitorPoints[0];
        Point topRight = monitorPoints[1];
        Point bottomLeft = monitorPoints[2];
        Point bottomRight = monitorPoints[3];

        Rectangle rectangle = new Rectangle(topLeft,topRight,bottomRight,bottomLeft);

        Rect outerMonitorRect = rectangle.getOuterRect();
        Log.w(TAG, "outerMonitorRect :");
        Log.d(TAG, "bottom = " + outerMonitorRect.bottom + " top " + outerMonitorRect.top);
        Log.d(TAG, "left = " + outerMonitorRect.left + " right " + outerMonitorRect.right);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        //Returns null, sizes are in the options variable
        BitmapFactory.decodeFile(filePath, options);
        int imageWidth = options.outWidth;
        int imageHeight = options.outHeight;

        Rect wholeWallImageRect = measureWholeWallImageSize(wallWidth,wallHeight,imageWidth,imageHeight);
        Log.w(TAG, "wholeWallImageRect :");
        Log.d(TAG, "bottom = " + wholeWallImageRect.bottom + " top " + wholeWallImageRect.top);
        Log.d(TAG, "left = " + wholeWallImageRect.left + " right " + wholeWallImageRect.right);

        ratio = (double)imageWidth / (double)wholeWallImageRect.width();
        Log.d(TAG, "imageWidth = " + imageWidth + " wholeWallImageRect.width() " + wholeWallImageRect.width());
        Log.d(TAG, "imageHeight = " + imageHeight + " wholeWallImageRect.height() " + wholeWallImageRect.height());
        Rect realImageCuttingRect = calculateImageCuttingRect(ratio,outerMonitorRect,wholeWallImageRect,
                imageHeight,imageWidth);


        options = new BitmapFactory.Options();
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inDither = false;
        options.inTempStorage = new byte[32 * 1024];

        Bitmap bitmap = null;
        try {
            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(filePath,false);
            bitmap = decoder.decodeRegion(realImageCuttingRect, options);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e ){
            Log.e(TAG,e.getMessage());
        }
        setImageBitmap(bitmap);
    }

    private static Rect calculateImageCuttingRect(
            double ratioRealDivideNew,Rect outerMonitorRect, Rect wholeWallImageRect, int imageHeight,int imageWidth){

        int indentLeft = wholeWallImageRect.left;
        int indentBottom = wholeWallImageRect.bottom;


//        int leftX = outerMonitorRect.left - wholeWallImageRect.left;
//        int rightX =/* wholeWallImageRect.width() - */(outerMonitorRect.right - wholeWallImageRect.right);
//        int topY =  (outerMonitorRect.top - wholeWallImageRect.top) - wholeWallImageRect.height() ;
//        int bottomY = outerMonitorRect.bottom - wholeWallImageRect.bottom;
        Rect imagePart =  getImagePartInMonitorRect(outerMonitorRect,wholeWallImageRect, indentLeft, indentBottom);
        int leftX = imagePart.left;
        int rightX =imagePart.right;
        int topY =  imagePart.top;
        int bottomY = imagePart.bottom;

        Log.w(TAG, "calculating before ratio adding :");
        Log.d(TAG, "bottom = " + bottomY + " top " + topY);
        Log.d(TAG, "left = " + leftX + " right " + rightX);

        leftX = (int) (leftX * ratioRealDivideNew);
        leftX = leftX < 0 ? 0 : leftX;
        rightX = (int) (rightX * ratioRealDivideNew);
        rightX = rightX > imageWidth ? imageWidth : rightX;
        topY = (int) (topY * ratioRealDivideNew);
        topY = topY > imageHeight ? imageHeight : topY;
        bottomY = (int) (bottomY * ratioRealDivideNew);
        bottomY = bottomY < 0 ? 0 : bottomY;
        Log.w(TAG, "realImageCuttingRect :");
        Log.d(TAG, "bottom = " + bottomY + " top " + topY);
        Log.d(TAG, "left = " + leftX + " right " + rightX);
        return new Rect(leftX,bottomY,rightX,topY);
    }

    private static Rect getImagePartInMonitorRect(Rect monitorRect, Rect imageRect, int indentLeft, int indentBottom) {
        Rect rect = new Rect();
        rect.left = monitorRect.left < imageRect.left ? imageRect.left : monitorRect.left;
        rect.bottom = monitorRect.bottom < imageRect.bottom ? imageRect.bottom : monitorRect.bottom;
        rect.right = monitorRect.right > imageRect.right ? imageRect.right : monitorRect.right;
        rect.top = monitorRect.top > imageRect.top ? imageRect.top : monitorRect.top;
        Log.w(TAG, "ImagePartInMonitorRect :");
        Log.d(TAG, "bottom = " + rect.bottom + " top " + rect.top);
        Log.d(TAG, "left = " + rect.left + " right " + rect.right);
        rect.left = rect.left - indentLeft;
        rect.right = rect.right - indentLeft;
        rect.bottom = rect.bottom - indentBottom;
        rect.top = rect.top - indentBottom;
        Log.w(TAG, "ImagePartInMonitorRect after indent :");
        Log.d(TAG, "bottom = " + rect.bottom + " top " + rect.top);
        Log.d(TAG, "left = " + rect.left + " right " + rect.right);
        return rect;
    }


    private static Rect measureWholeWallImageSize(int wallWidth, int wallHeight, int imageWidth, int imageHeight) {
        if (wallWidth == 0 || wallHeight == 0 ) {
            Log.w("AspectRatioImageView", "(width == 0 || parentHeight == 0");
            return null;
        }

        int originalWallWidth = wallWidth;
        int originalWallHeight = wallHeight;
        try {
            float imageSideRatio = (float)imageWidth / (float)imageHeight;
            float viewSideRatio = (float)wallWidth / (float)wallHeight;
            if (imageSideRatio <= viewSideRatio) {
                // Image is taller than the display (ratio)
                if ( viewSideRatio > 1 ) { // Wide monitor
                    if (imageSideRatio > 1) { // Wide picture
//                            Log.w(TAG, "*** 1 ***");
                        wallHeight = (int)(wallWidth / imageSideRatio);
                        return getImageRect(originalWallWidth,originalWallHeight,wallWidth,wallHeight);
                    } else { // Tall picture
//                            Log.w(TAG, "*** 2 ***");
                        wallWidth = (int)(wallHeight * imageSideRatio);
                        return getImageRect(originalWallWidth,originalWallHeight,wallWidth,wallHeight);
                    }
                } else { // Tall monitor
                    if (imageSideRatio > 1) { // Wide picture
//                            Log.w(TAG, "*** 3 ***");
                        Log.e(TAG, "This is not real");
                        wallHeight = (int)(wallWidth / imageSideRatio);
                        return getImageRect(originalWallWidth,originalWallHeight,wallWidth,wallHeight);
                    } else { // Tall picture
//                            Log.w(TAG, "*** 4 ***");
                        wallHeight = (int)(wallWidth / imageSideRatio);
                        return getImageRect(originalWallWidth,originalWallHeight,wallWidth,wallHeight);
                    }
                }
            } else {
                // Image is wider than the display (ratio)
                if ( viewSideRatio > 1 ) { // Wide monitor
                    if (imageSideRatio > 1) { // Wide picture
//                            Log.w(TAG, "*** 5 ***");
                        wallWidth = (int)(wallHeight * imageSideRatio);
                        return getImageRect(originalWallWidth,originalWallHeight,wallWidth,wallHeight);
                    } else { // Tall picture
//                            Log.w(TAG, "*** 6 ***");
                        Log.e(TAG, "This is not real");
                        wallWidth = (int)(wallHeight * imageSideRatio);
                        return getImageRect(originalWallWidth,originalWallHeight,wallWidth,wallHeight);
                    }
                } else { // Tall monitor
                    if (imageSideRatio > 1) { // Wide picture
//                            Log.w(TAG, "*** 7 ***");
                        wallHeight = (int)(wallWidth / imageSideRatio);
                        return getImageRect(originalWallWidth,originalWallHeight,wallWidth,wallHeight);
                    } else { // Tall picture
//                            Log.w(TAG, "*** 8 ***");
                        wallWidth = (int)(wallHeight * imageSideRatio);

                        return getImageRect(originalWallWidth,originalWallHeight,wallWidth,wallHeight);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
        return null;
    }

    private static Rect getImageRect(int originalWallWidth, int originalWallHeight, int imageWidth, int imageHeight){
        int leftX = originalWallWidth / 2 - imageWidth / 2;
        int topY = originalWallHeight / 2 + imageHeight/ 2;
        int rightX = originalWallWidth / 2 + imageWidth / 2;
        int bottomY = originalWallHeight / 2 - imageHeight / 2;
        return new Rect(leftX,topY,rightX,bottomY);
    }

}
