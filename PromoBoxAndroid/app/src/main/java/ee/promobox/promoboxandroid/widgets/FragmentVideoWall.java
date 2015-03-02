package ee.promobox.promoboxandroid.widgets;

import android.app.Fragment;

import ee.promobox.promoboxandroid.data.CampaignFile;

/**
 * Created by ilja on 27.02.2015.
 */
public abstract class FragmentVideoWall extends Fragment {

    public abstract void playFile(CampaignFile campaignFile, long frameId);
    public abstract void prepareFile(CampaignFile campaignFile);

}
