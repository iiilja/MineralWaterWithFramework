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
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.IOException;

import ee.promobox.promoboxandroid.data.ErrorMessage;
import ee.promobox.promoboxandroid.intents.ErrorMessageIntent;
import ee.promobox.promoboxandroid.intents.ToastIntent;
import ee.promobox.promoboxandroid.util.geom.Rectangle;

/**
 * Created by ilja on 19.02.2015.
 */
public class WallImageView extends ImageView{


    private static final String TAG = "VideoWallImageView";

    private static final int[][] gravitiesRelative = {
            {RelativeLayout.ALIGN_PARENT_TOP , RelativeLayout.ALIGN_PARENT_LEFT , RelativeLayout.ALIGN_PARENT_START},
            {RelativeLayout.ALIGN_PARENT_TOP , RelativeLayout.CENTER_HORIZONTAL},
            {RelativeLayout.ALIGN_PARENT_TOP , RelativeLayout.ALIGN_PARENT_RIGHT , RelativeLayout.ALIGN_PARENT_END},
            {RelativeLayout.CENTER_VERTICAL , RelativeLayout.ALIGN_PARENT_RIGHT , RelativeLayout.ALIGN_PARENT_END},
            {RelativeLayout.ALIGN_PARENT_BOTTOM , RelativeLayout.ALIGN_PARENT_RIGHT , RelativeLayout.ALIGN_PARENT_END},
            {RelativeLayout.ALIGN_PARENT_BOTTOM , RelativeLayout.CENTER_HORIZONTAL},
            {RelativeLayout.ALIGN_PARENT_BOTTOM , RelativeLayout.ALIGN_PARENT_LEFT , RelativeLayout.ALIGN_PARENT_START},
            {RelativeLayout.CENTER_VERTICAL , RelativeLayout.ALIGN_PARENT_LEFT , RelativeLayout.ALIGN_PARENT_START},
            {RelativeLayout.CENTER_VERTICAL , RelativeLayout.CENTER_HORIZONTAL}
    };

    private LocalBroadcastManager bManager;
    private double ratio;

    public WallImageView(Context context) {
        super(context);
        bManager = LocalBroadcastManager.getInstance(context);
    }

    public WallImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        bManager = LocalBroadcastManager.getInstance(context);
    }

    public WallImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        bManager = LocalBroadcastManager.getInstance(context);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (widthMeasureSpec == 0 || heightMeasureSpec == 0
                || MeasureSpec.getSize(widthMeasureSpec) == 0 || MeasureSpec.getSize(heightMeasureSpec) == 0) {
            Log.w("AspectRatioImageView","(widthMeasureSpec == 0 || heightMeasureSpec == 0" );
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        try {
//            Log.w(TAG, "onMeasure");
            Drawable drawable = getDrawable();

            if (drawable == null) {
                setMeasuredDimension(0, 0);
            } else {
                float imageSideRatio = (float)drawable.getIntrinsicWidth() / (float)drawable.getIntrinsicHeight();
                float viewSideRatio = (float)MeasureSpec.getSize(widthMeasureSpec) / (float)MeasureSpec.getSize(heightMeasureSpec);
                int width;
                int height;
                if (imageSideRatio <= viewSideRatio) {
                    // Image is taller than the display (ratio)
                    if ( viewSideRatio > 1 ) { // Wide monitor
                        if (imageSideRatio > 1) { // Wide picture
                            Log.w(TAG, "*** 1 ***");
                            width = MeasureSpec.getSize(widthMeasureSpec);
                            height = (int)(width / imageSideRatio);
                            setMeasuredDimension(width, height);
                        } else { // Tall picture
                            Log.w(TAG, "*** 2 ***");
                            height = MeasureSpec.getSize(heightMeasureSpec);
                            width = (int)(height * imageSideRatio);
                            setMeasuredDimension(width, height);
                        }
                    } else { // Tall monitor
                        if (imageSideRatio > 1) { // Wide picture
                            Log.w(TAG, "*** 3 ***");
                            Log.e(TAG, "This is not real");
                            width = MeasureSpec.getSize(widthMeasureSpec);
                            height = (int)(width / imageSideRatio);
                            setMeasuredDimension(width, height);
                        } else { // Tall picture
                            Log.w(TAG, "*** 4 ***");
                            height = MeasureSpec.getSize(heightMeasureSpec);
                            width = (int)(height * imageSideRatio);
                            setMeasuredDimension(width, height);
                        }
                    }
                } else {
                    // Image is wider than the display (ratio)
                    if ( viewSideRatio > 1 ) { // Wide monitor
                        if (imageSideRatio > 1) { // Wide picture
                            Log.w(TAG, "*** 5 ***");
                            width = MeasureSpec.getSize(widthMeasureSpec);
                            height = (int)(width / imageSideRatio);
                            setMeasuredDimension(width, height);
                        } else { // Tall picture
                            Log.w(TAG, "*** 6 ***");
                            Log.e(TAG, "This is not real");
                            height = MeasureSpec.getSize(heightMeasureSpec);
                            width = (int)(height * imageSideRatio);
                            setMeasuredDimension(width, height);
                        }
                    } else { // Tall monitor
                        if (imageSideRatio > 1) { // Wide picture
                            Log.w(TAG, "*** 7 ***");
                            width = MeasureSpec.getSize(widthMeasureSpec);
                            height = (int)(width / imageSideRatio);
                            setMeasuredDimension(width, height);
                        } else { // Tall picture
                            Log.w(TAG, "*** 8 ***");
                            height = MeasureSpec.getSize(heightMeasureSpec);
                            width = (int)(height * imageSideRatio);
                            setMeasuredDimension(width, height);
                        }
                    }
                }
            }
        } catch (Exception e) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void setImageDrawable(String filePath, int wallHeight, int wallWidth, Point[] monitorPoints) {
        monitorPoints[0] = new Point(1080,1920);
        monitorPoints[1] = new Point(1080,0);
        monitorPoints[2] = new Point(0,0);
        monitorPoints[3] = new Point(0,1920);

//        monitorPoints[0] = new Point(2160,1920);
//        monitorPoints[1] = new Point(2160,0);
//        monitorPoints[2] = new Point(1080,0);
//        monitorPoints[3] = new Point(1080,1920);

//        monitorPoints[0] = new Point(3240,1920);
//        monitorPoints[1] = new Point(3240,0);
//        monitorPoints[2] = new Point(2160,0);
//        monitorPoints[3] = new Point(2160,1920);

        Point topLeft = monitorPoints[0];
        Point topRight = monitorPoints[1];
        Point bottomLeft = monitorPoints[2];
        Point bottomRight = monitorPoints[3];

        Rectangle rectangle = new Rectangle(topLeft,topRight,bottomRight,bottomLeft);

        Log.d(TAG, " Rotation =" + rectangle.getAvgAngle());
        setRotation((float) rectangle.getAvgAngle());

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
            if (realImageCuttingRect.width() != 0 && realImageCuttingRect.height() != 0){
                BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(filePath,false);
                bitmap = decoder.decodeRegion(realImageCuttingRect, options);
            }
        } catch (IOException e) {
            bManager.sendBroadcast(new ToastIntent("Error finding image"));
            bManager.sendBroadcast(new ErrorMessageIntent(e));
            e.printStackTrace();
        } catch (IllegalArgumentException e ){
//            bManager.sendBroadcast(new ToastIntent("Error cutting image"));
//            bManager.sendBroadcast(new ErrorMessageIntent(e));
            Log.e(TAG,e.getMessage());
        }
        setImageBitmap(bitmap);
    }

    private Rect calculateImageCuttingRect(
            double ratioRealDivideNew,Rect outerMonitorRect, Rect wholeWallImageRect, int imageHeight,int imageWidth){

        int indentLeft = wholeWallImageRect.left;
        int indentBottom = wholeWallImageRect.bottom;


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

    private Rect getImagePartInMonitorRect(Rect monitorRect, Rect imageRect, int indentLeft, int indentBottom) {
        Rect rect = new Rect();
        boolean moveToRight = false;
        boolean moveToLeft = false;
        boolean moveUp = false;
        boolean moveDown = false;


        if (monitorRect.left < imageRect.left) {
            rect.left = imageRect.left;
            moveToRight = true;
        } else {
            rect.left = monitorRect.left;
        }
        if (monitorRect.bottom < imageRect.bottom) {
            rect.bottom = imageRect.bottom;
            moveUp = true;
        } else {
            rect.bottom = monitorRect.bottom;
        }
        if (monitorRect.right > imageRect.right) {
            rect.right = imageRect.right;
            moveToLeft = true;
        } else {
            rect.right = monitorRect.right;
        }
        if (monitorRect.top > imageRect.top) {
            rect.top = imageRect.top;
            moveDown = true;
        } else {
            rect.top = monitorRect.top;
        }

        int gravity[] = gravitiesRelative[8];

        Log.d(TAG, " moveUp = "+moveUp+" moveToRight = "+moveToRight+" moveDown = "+moveDown+" moveToLeft = "+moveToLeft);

        if (moveUp && moveToRight && moveDown && moveToLeft) {
            Log.e(TAG, "Move to center, cant be possible");
        }
        if (moveUp && !moveToRight && !moveDown && moveToLeft) {
            Log.d(TAG,"Top - Left");
            gravity = gravitiesRelative[0];
        }
        if (moveUp && !moveDown && (moveToLeft && moveToRight || !moveToLeft && !moveToRight)) {
            Log.d(TAG,"Top - Center");
            gravity = gravitiesRelative[1];
        }
        if (moveUp && moveToRight && !moveDown && !moveToLeft) {
            Log.d(TAG,"Top - Right");
            gravity = gravitiesRelative[2];
        }
        if (moveToRight && !moveToLeft && ( moveUp && moveDown || !moveUp && !moveDown)) {
            Log.d(TAG,"Right - Center");
            gravity = gravitiesRelative[3];
        }
        if (!moveUp && moveToRight && moveDown && !moveToLeft) {
            Log.d(TAG,"Bottom - Right");
            gravity = gravitiesRelative[4];
        }
        if (!moveUp && moveDown && ( moveToRight  && moveToLeft || !moveToRight  && !moveToLeft)) {
            Log.d(TAG,"Bottom - Center");
            gravity = gravitiesRelative[5];
        }
        if (!moveUp && !moveToRight && moveDown && moveToLeft) {
            Log.d(TAG,"Bottom - Left");
            gravity = gravitiesRelative[6];
        }
        if (!moveToRight && moveToLeft && ( moveDown && moveUp || !moveDown && !moveUp )) {
            Log.d(TAG,"Left - Center");
            gravity = gravitiesRelative[7];
        }
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
//        for (int i = 0; i < 22; i++) {
//            params.removeRule(i);
//        }
        for (int i = 0; i < gravity.length; i++) {
            params.addRule(gravity[i]);
        }
        setLayoutParams(params);

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
