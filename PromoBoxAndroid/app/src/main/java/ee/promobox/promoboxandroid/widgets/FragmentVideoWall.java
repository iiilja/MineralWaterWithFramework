package ee.promobox.promoboxandroid.widgets;

import android.graphics.Point;

import ee.promobox.promoboxandroid.data.CampaignFile;

/**
 * Created by ilja on 27.02.2015.
 */
public abstract class FragmentVideoWall extends FragmentWithSeekBar {

    protected static final int DEFAULT_WIDTH = 1920;
    protected static final int DEFAULT_HEIGHT = 1080;
    protected static final Point[] DEFAULT_POINTS = {
            new Point(0,1080),
            new Point(1920,1080),
            new Point(1920,0),
            new Point(0,0)
    };

    public abstract void playFile(CampaignFile campaignFile, long frameId);

    public abstract void prepareFile(CampaignFile campaignFile);

}
