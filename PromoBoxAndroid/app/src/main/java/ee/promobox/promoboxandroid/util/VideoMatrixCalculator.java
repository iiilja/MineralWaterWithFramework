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
     */
    private static double calculateInitialRatio(WallData wallData){
        double scaleY = (double)wallData.monitorHeight / wallData.videoHeight;
        double scaleX = (double)wallData.monitorWidth / wallData.videoWidth;
        double ratio = scaleX/scaleY;
        Log.w(TAG, "***1***");
        Log.d(TAG, "scaleY = "+scaleY+" scaleX = "+ scaleX + " ratio = " + ratio);
        return ratio;
    }

    private static float calculateScaleIfRatiosNotEqual(WallData wallData){
        float ratioSrc = wallData.src.width() / wallData.src.height();
        float ratioVideo = (float)wallData.videoWidth / wallData.videoHeight;

        if (ratioSrc > ratioVideo){
            return 1;
        } else {
            // Video is wider
            return ratioVideo/ratioSrc;
        }
    }

    private static float[] calculateTranslationIfRatiosNotEqual(WallData wallData){
        float ratioSrc = wallData.src.width() / wallData.src.height();
        float ratioVideo = (float)wallData.videoWidth / wallData.videoHeight;

        if (ratioSrc > ratioVideo){
            float vidWidth = wallData.src.height()*ratioVideo;
            return new float[]{(wallData.src.width() - vidWidth)/2,0};
        } else {
            // Video is wider
            float vidHeight = wallData.src.width()/ratioVideo;

            return new float[]{0,(wallData.src.height()-vidHeight)/2};
        }
    }


    public static float[] calculateScaleXY(WallData wallData){
        
        double initialScale = calculateInitialRatio(wallData);


        // TODO: this wil lbe fine only if video and wall ratios are equal
        float scaleRatiosNotEqual = calculateScaleIfRatiosNotEqual(wallData);

        float scaleY = wallData.src.height()/(wallData.monitorHeight * scaleRatiosNotEqual);
        float scaleX = (float) (scaleY / initialScale);
        Log.d(TAG, "scaleY = "+scaleY+" scaleX = "+ scaleX);
        return new float[]{scaleX,scaleY};
    }
    
    public static float[] calculateTranslationXY(WallData wallData){
        Point[] points = wallData.getMonitorPoints();
        int monitorWidth = wallData.getMonitorWidth();
        int monitorHeight = wallData.getMonitorHeight();
        float dResolutionX = (float) (Line.getLineLength(points[0],points[1]) / monitorWidth);
        float dResolutionY = (float) (Line.getLineLength(points[1], points[2]) / monitorHeight);

        float[] translateIfRatioNotEqual = calculateTranslationIfRatiosNotEqual(wallData);

        float dstX = wallData.dst.centerX() - translateIfRatioNotEqual[0];
        float dstY = wallData.dst.centerY() - translateIfRatioNotEqual[1];

        float dX = ((float)monitorWidth/2 - dstX)/dResolutionX;
        float dY = ((float)monitorHeight/2 - dstY)/dResolutionY;

        Log.d(TAG,"monitorWidth = " + monitorWidth + " dstX = " + dstX + " dResolutionX" + dResolutionX);
        Log.d(TAG,"monitorHeight = " + monitorHeight + " dstY = " + dstY + " dResolutionY" + dResolutionY);

        Log.d(TAG, "Translation dX = " + dX + " dY = " + dY);

        return new float[]{dX,dY};
    }

    public static class WallData{

        private int videoWidth;
        private int videoHeight;
        private int monitorWidth;
        private int monitorHeight;
        private Point[] monitorPoints;
        private RectF dst;
        private RectF src;

        public WallData(Point[] points, RectF src, RectF dst, int monitorWidth, int monitorHeight, int videoWidth, int videoHeight){
            this.monitorPoints = points;
            this.src = src;
            this.dst = dst;
            this.monitorWidth = monitorWidth;
            this.monitorHeight = monitorHeight;
            this.videoWidth = videoWidth;
            this.videoHeight = videoHeight;
        }

        public int getMonitorHeight() {
            return monitorHeight;
        }

        public int getMonitorWidth() {
            return monitorWidth;
        }

        public int getVideoHeight() {
            return videoHeight;
        }

        public int getVideoWidth() {
            return videoWidth;
        }

        public Point[] getMonitorPoints() {
            return monitorPoints;
        }

        public RectF getDst() {
            return dst;
        }

        public RectF getSrc() {
            return src;
        }
    }


}
