package ee.promobox.promoboxandroid.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.IOException;

import ee.promobox.promoboxandroid.intents.ErrorMessageIntent;
import ee.promobox.promoboxandroid.intents.ToastIntent;
import ee.promobox.promoboxandroid.util.geom.Rectangle;

/**
 * Created by ilja on 19.02.2015.
 */
public class WallImageView extends ImageView{

    private float wholeViewRotation;


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
    private int monitorVirtualWidth;
    private int monitorVirtualHeight;

    private RelativeLayout.LayoutParams futureLayoutParams;

    private Rect outerMonitorRect;
    private int wallWidth;
    private int wallHeight;

    private Integer measuredWidth;
    private Integer measuredHeight;

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
        if (measuredHeight == null || measuredWidth == null){
            measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
            measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        }
        try {
//            Log.w(TAG, "onMeasure");
            Drawable drawable = getDrawable();

            if (drawable == null) {
                setMeasuredDimension(0, 0);
            } else {
                int width, height;
                width = measuredWidth;
                height = measuredHeight;
//                Log.d(TAG,"Measured width = " + width + " measuredHeight = " +height );
                width = width > height ? width : height;
                int virtualWidth = monitorVirtualWidth > monitorVirtualHeight ? monitorVirtualWidth : monitorVirtualHeight;
//                Log.d(TAG,"width to ratioNew = " + width + "monitorVirtualwidth = " +virtualWidth );
                double ratioNew = (double)width/(double)virtualWidth;
                int drawableWidth = drawable.getIntrinsicWidth();
                int drawableHeight = drawable.getIntrinsicHeight();
                width = (int) (drawableWidth * ratioNew / ratio);
                height = (int) (drawableHeight * ratioNew / ratio);
//                Log.d(TAG, "Setting dimension width = " + width + " height = " + height +" with ratioNew = "+ratioNew+" /ratio = " + ratio);
                if (width != 0 && height != 0){
                    setMeasuredDimension(width, height);
                }
                else {
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }
            }
        } catch (Exception e) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void setInitialValues(int wallHeight, int wallWidth, Point[] monitorPoints){
        Point topLeft = monitorPoints[0];
        Point topRight = monitorPoints[1];
        Point bottomRight = monitorPoints[2];
        Point bottomLeft = monitorPoints[3];

        this.wallHeight = wallHeight;
        this.wallWidth = wallWidth;

        Rectangle rectangle = new Rectangle(topLeft,topRight,bottomRight,bottomLeft);

        Log.d(TAG, " Rotation =" + rectangle.getAvgAngle());
//        setRotation((float) -rectangle.getAvgAngle());

        outerMonitorRect = rectangle.getOuterRect();
//        Log.w(TAG, "outerMonitorRect :");
//        Log.d(TAG, "bottom = " + outerMonitorRect.bottom + " top " + outerMonitorRect.top);
//        Log.d(TAG, "left = " + outerMonitorRect.left + " right " + outerMonitorRect.right);
        monitorVirtualWidth = Math.abs(outerMonitorRect.width());
        monitorVirtualHeight = Math.abs(outerMonitorRect.height());
    }

    public Bitmap getImageBitmap(String filePath) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        //Returns null, sizes are in the options variable
        BitmapFactory.decodeFile(filePath, options);
        int imageWidth = options.outWidth;
        int imageHeight = options.outHeight;

        Rect wholeWallImageRect = measureWholeWallImageSize(wallWidth,wallHeight,imageWidth,imageHeight);
//        Log.w(TAG, "wholeWallImageRect :");
//        Log.d(TAG, "bottom = " + wholeWallImageRect.bottom + " top " + wholeWallImageRect.top);
//        Log.d(TAG, "left = " + wholeWallImageRect.left + " right " + wholeWallImageRect.right);

        ratio = (double)imageWidth / (double)wholeWallImageRect.width();
//        Log.d(TAG, "imageWidth = " + imageWidth + " wholeWallImageRect.width() " + wholeWallImageRect.width());
//        Log.d(TAG, "imageHeight = " + imageHeight + " wholeWallImageRect.height() " + wholeWallImageRect.height());

        Rect imagePart =  getImagePartInMonitorRect(outerMonitorRect,wholeWallImageRect);

//        Log.w(TAG, "*** ImagePart");
//        Log.d(TAG, "bottom = " + imagePart.bottom + " top " + imagePart.top);
//        Log.d(TAG, "left = " + imagePart.left + " right " + imagePart.right);
//
//        Log.w(TAG, "*** OuterMonitorRect");
//        Log.d(TAG, "bottom = " + outerMonitorRect.bottom + " top " + outerMonitorRect.top);
//        Log.d(TAG, "left = " + outerMonitorRect.left + " right " + outerMonitorRect.right);

        // Rects to create transparent bitmap
        Rect outerMonitorRectCopy = copyRect(outerMonitorRect);
        Rect imagePartRectCopy = copyRect(imagePart);
        toZeroXY(outerMonitorRectCopy,imagePartRectCopy);

        int transparentWidth = (int) Math.abs(outerMonitorRectCopy.width() * ratio);
        int transparentHeight = (int) Math.abs(outerMonitorRectCopy.height() * ratio);
        float overlayLeft = (float) (imagePartRectCopy.left * ratio);
        float overlayTop = (float) (imagePartRectCopy.bottom * ratio);

        boolean shouldOverlay = imagePartRectCopy.width() != outerMonitorRectCopy.width() ||
                imagePartRectCopy.height() != outerMonitorRectCopy.height();

        Bitmap transparentBitmap = null;
        if (shouldOverlay){
            transparentBitmap = Bitmap.createBitmap(transparentWidth,transparentHeight, Bitmap.Config.ARGB_8888);
        }

//        Log.w(TAG, "*** ImagePart TOZERO");
//        Log.d(TAG, "bottom = " + imagePartRectCopy.bottom + " top " + imagePartRectCopy.top);
//        Log.d(TAG, "left = " + imagePartRectCopy.left + " right " + imagePartRectCopy.right);
//
//        Log.w(TAG, "*** OuterMonitorRect TOZERO");
//        Log.d(TAG, "bottom = " + outerMonitorRectCopy.bottom + " top " + outerMonitorRectCopy.top);
//        Log.d(TAG, "left = " + outerMonitorRectCopy.left + " right " + outerMonitorRectCopy.right);

        int indentLeft = wholeWallImageRect.left;
        int indentBottom = wholeWallImageRect.bottom;

        imagePart.left = imagePart.left - indentLeft;
        imagePart.right = imagePart.right - indentLeft;
        imagePart.bottom = imagePart.bottom - indentBottom;
        imagePart.top = imagePart.top - indentBottom;

//        Log.w(TAG, "ImagePartInMonitorRect after indent :");
//        Log.d(TAG, "bottom = " + imagePart.bottom + " top " + imagePart.top);
//        Log.d(TAG, "left = " + imagePart.left + " right " + imagePart.right);

        Rect realImageCuttingRect = calculateImageCuttingRect(imagePart,ratio,imageHeight,imageWidth);


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
                if (shouldOverlay){
                    bitmap = overlay(transparentBitmap,bitmap,overlayLeft,overlayTop);
                }
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
        return bitmap;
    }

    private void toZeroXY(Rect outerMonitorRectCopy, Rect imagePartRectCopy) {
        int indentLeft = outerMonitorRectCopy.left;
        int indentBottom = outerMonitorRectCopy.bottom;

        outerMonitorRectCopy.left -= indentLeft;
        outerMonitorRectCopy.right -= indentLeft;
        outerMonitorRectCopy.bottom -= indentBottom;
        outerMonitorRectCopy.top -= indentBottom;
        imagePartRectCopy.right -= indentLeft;
        imagePartRectCopy.left -= indentLeft;
        imagePartRectCopy.bottom -= indentBottom;
        imagePartRectCopy.top -= indentBottom;

    }

    private Rect copyRect(Rect rect) {
        return new Rect(rect.left,rect.top,rect.right,rect.bottom);
    }

    private Rect calculateImageCuttingRect( Rect imagePart,
            double ratioRealDivideNew,int imageHeight,int imageWidth){


        int leftX = imagePart.left;
        int rightX =imagePart.right;
        int topY =  imagePart.top;
        int bottomY = imagePart.bottom;

        leftX = (int) (leftX * ratioRealDivideNew);
        leftX = leftX < 0 ? 0 : leftX;
        rightX = (int) (rightX * ratioRealDivideNew);
        rightX = rightX > imageWidth ? imageWidth : rightX;
        topY = (int) (topY * ratioRealDivideNew);
        topY = topY > imageHeight ? imageHeight : topY;
        bottomY = (int) (bottomY * ratioRealDivideNew);
        bottomY = bottomY < 0 ? 0 : bottomY;

//        Log.w(TAG, "realImageCuttingRect :");
//        Log.d(TAG, "bottom = " + bottomY + " top " + topY);
//        Log.d(TAG, "left = " + leftX + " right " + rightX);
        return new Rect(leftX,bottomY,rightX,topY);
    }

    private Rect getImagePartInMonitorRect(Rect monitorRect, Rect imageRect) {
        Rect rect = new Rect();

        if (monitorRect.left < imageRect.left) {
            rect.left = imageRect.left;
        } else {
            rect.left = monitorRect.left;
        }
        if (monitorRect.bottom < imageRect.bottom) {
            rect.bottom = imageRect.bottom;
        } else {
            rect.bottom = monitorRect.bottom;
        }
        if (monitorRect.right > imageRect.right) {
            rect.right = imageRect.right;
        } else {
            rect.right = monitorRect.right;
        }
        if (monitorRect.top > imageRect.top) {
            rect.top = imageRect.top;
        } else {
            rect.top = monitorRect.top;
        }

//        Log.w(TAG, "ImagePartInMonitorRect :");
//        Log.d(TAG, "bottom = " + rect.bottom + " top " + rect.top);
//        Log.d(TAG, "left = " + rect.left + " right " + rect.right);

        return rect;
    }


    private static Rect measureWholeWallImageSize(int wallWidth, int wallHeight, int imageWidth, int imageHeight) {
        if (wallWidth == 0 || wallHeight == 0 ) {
            Log.w("AspectRatioImageView", "(width == 0 || parentHeight == 0");
            Log.w("AspectRatioImageView", "Returning empty rect");
            return new Rect();
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

    public static Bitmap overlay(Bitmap bmp1, Bitmap bmp2, float left, float top) {
        Log.d(TAG, "BITMAP OVERLAY left = " + left+ " top = " + top);
        //Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmp1);
//        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, left, top, null);
        return bmp1;
    }


    private static Rect getImageRect(int originalWallWidth, int originalWallHeight, int imageWidth, int imageHeight){
        int leftX = originalWallWidth / 2 - imageWidth / 2;
        int topY = originalWallHeight / 2 + imageHeight/ 2;
        int rightX = originalWallWidth / 2 + imageWidth / 2;
        int bottomY = originalWallHeight / 2 - imageHeight / 2;
        return new Rect(leftX,topY,rightX,bottomY);
    }

    public float getWholeViewRotation() {
        return wholeViewRotation;
    }
}
