package ee.promobox.promoboxandroid.util;

import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ee.promobox.promoboxandroid.MainActivity;
import ee.promobox.promoboxandroid.data.ErrorMessage;
import ee.promobox.promoboxandroid.util.geom.Line;

/**
 * Created by ilja on 25.03.2015.
 */
public class VideoMatrixCalculator {
    private static final String TAG = "VideoMatrixCalculator";


    public static Point calculateVideoSize(String pathToFile) {
        try {
            File file = new File(pathToFile);
            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
            metaRetriever.setDataSource(file.getAbsolutePath());
            String height = metaRetriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            String width = metaRetriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            Log.d(TAG, height + "h , w = " + width);
            Point size = new Point();
            size.set(Integer.parseInt(width), Integer.parseInt(height));
            return size;

        } catch (NumberFormatException ex) {
            Log.e(TAG, ex.getMessage());
        }
        return new Point(0,0);
    }

    public static Point calculateNeededVideoSize(Point videoSize, int viewHeight, int viewWidth) {
        int videoHeight = videoSize.y;
        int videoWidth = videoSize.x;
        float imageSideRatio = (float)videoWidth / (float)videoHeight;
        float viewSideRatio = (float) viewWidth / (float) viewHeight;
        if (imageSideRatio > viewSideRatio) {
            // Image is taller than the display (ratio)
            int height = (int)(viewWidth / imageSideRatio);
            videoSize.set(viewWidth, height);
        } else {
            // Image is wider than the display (ratio)
            int width = (int)(viewHeight * imageSideRatio);
            videoSize.set(width, viewHeight);
        }
        return videoSize;
    }




    /**
     * Video is scaled to full screen on start, so we should measure how much it was scaled.
     * @param videoWidth
     * @param videoHeight
     * @param monitorWidth
     * @param monitorHeight
     * @return
     */
    private static double calculateInitialRatio(int videoWidth, int videoHeight, int monitorWidth, int monitorHeight){
        double scaleY = (double)monitorHeight / videoHeight;
        double scaleX = (double)monitorWidth / videoWidth;
        double ratio = scaleX/scaleY;
        Log.w(TAG, "***1***");
        Log.d(TAG, "scaleY = "+scaleY+" scaleX = "+ scaleX + " retio = " + ratio);
        return ratio;
    }


    public static float[] calculateScaleXY(int videoWidth, int videoHeight, int monitorWidth,int monitorHeight, RectF wall){
        
        double initialScale = calculateInitialRatio(videoWidth,videoHeight,monitorWidth,monitorHeight);
        
        // TODO: this wil lbe fine only if video and wall ratios are equal
        float scaleY = wall.height()/monitorHeight;
        float scaleX = (float) (scaleY / initialScale);
        return new float[]{scaleX,scaleY};
    }
    
    public static float[] calculateTranslationAndXY(Point[] points, RectF src, RectF dst, int monitorWidth, int monitorHeight){
        float dResolutionX = (float) (Line.getLineLength(points[0],points[1]) / monitorWidth);
        float dResolutionY = (float) (Line.getLineLength(points[1], points[2]) / monitorHeight);
        
//        float dstX = getLeftPositionByPoints(points);
//        float dstY = getTopPositionByPoints(points);

        float dstX = dst.centerX();
        float dstY = dst.centerY();

        float dX = ((float)monitorWidth/2 - dstX)/dResolutionX;
        float dY = ((float)monitorHeight/2 - dstY)/dResolutionY;

        Log.d(TAG,"monitorWidth = " + monitorWidth + " dstX = " + dstX + " dResolutionX" + dResolutionX);
        Log.d(TAG,"monitorHeight = " + monitorHeight + " dstY = " + dstY + " dResolutionY" + dResolutionY);

        Log.d(TAG, "Translation dX = " + dX + " dY = " + dY);


        return new float[]{dX,dY};
    }


}
