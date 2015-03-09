package ee.promobox.promoboxandroid.data;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ilja on 7.03.2015.
 */
public class CampaignMultiple extends Campaign {

    private static final String TAG = "CampaignMultiple ";

    private ArrayList<Campaign> campaigns;
    private int campaignPosition = 0;

    public CampaignMultiple(ArrayList<Campaign> campaigns){

        this.campaigns = campaigns;
        order  = ORDER_ASC;
        files = new ArrayList<>();
        delay = 5;
        for (Campaign campaign : campaigns){
            delay = delay > campaign.getDelay() ? delay : campaign.getDelay();
            if (campaign.getFiles() != null){
                files.addAll(campaign.getFiles());
            }
        }
    }

    @Override
    public int getCampaignId() {
        return campaigns.get(campaignPosition).getCampaignId();
    }

    @Override
    public String getCampaignName() {
        return campaigns.get(campaignPosition).getCampaignName();
    }


    @Override
    public int getDelay() {
        return campaigns.get(campaignPosition).getDelay();
    }


    @Override
    public CampaignFile getNextFile(){
        Log.d(TAG, "getNextFile() campaignPosition =" + campaignPosition  + "filename = " + campaigns.get(campaignPosition).getNextFile().getName());
        return campaigns.get(campaignPosition).getNextFile();
    }

    @Override
    public void setNextFilePosition(){
        Campaign campaign = campaigns.get(campaignPosition);
        position ++;
        campaign.setNextFilePosition();
        if (position >= campaign.files.size() ) {
            increaseCampaignPosition();
            campaign = campaigns.get(campaignPosition);
            campaign.position = -1;
            position = -1;
            if (files.size() > 0) {
                setNextFilePosition();
            }
        }
        Log.w(TAG, "POSITION = " + position);
        Log.w(TAG, "POSITION IN CAMPAIGN = " + campaigns.get(campaignPosition).position);
    }

    @Override
    public void setPreviousFilePosition(){
        Campaign campaign = campaigns.get(campaignPosition);
        position = position - 2;
        campaign.setPreviousFilePosition();
        if (position < 0 ) {
            campaign.position = 0;
            decreaseCampaignPosition();
            campaign = campaigns.get(campaignPosition);
            position = campaign.files.size() + position;
            campaign.position = position > 0 ? position : 0;
            if (position < 0 && files.size() > 0) {
                position = position - 2;
                setPreviousFilePosition();
            }
        }
        Log.w(TAG, "PREV POSITION = " + position);
        Log.w(TAG, "PREV POSITION IN CAMPAIGN = " + campaigns.get(campaignPosition).position);

    }

    private void increaseCampaignPosition(){
        campaignPosition ++;
        if (campaignPosition >= campaigns.size()){
            campaignPosition = 0;
        }
    }

    private void decreaseCampaignPosition(){
        campaignPosition --;
        if (campaignPosition < 0){
            campaignPosition = campaigns.size() - 1;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (!o.getClass().equals(getClass())){
            Log.d(TAG, "not same class");
            return false;
        }
        CampaignMultiple other = (CampaignMultiple) o;
        if (campaigns.size() != other.campaigns.size()) return false;
        for (Campaign campaign : campaigns ){
            if (!other.campaigns.contains(campaign)) return false;
        }
        return true;
    }
}
