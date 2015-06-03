package ee.promobox.promoboxandroid.data;

import android.os.Parcel;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ilja on 7.03.2015.
 */
public class CampaignMultiple extends Campaign {

    private static final String TAG = "CampaignMultiple ";

    private ArrayList<Campaign> campaigns;
    private JSONArray json;
    private int campaignPosition = 0;

    public CampaignMultiple(ArrayList<Campaign> campaigns){

        this.campaigns = campaigns;
        order  = ORDER_ASC;
        files = new ArrayList<>();
        for (Campaign campaign : campaigns){
            if (campaign.getFiles() != null){
                files.addAll(campaign.getFiles());
            }
        }
    }

    public CampaignMultiple(JSONArray campaignsJSON){
        try {
            campaigns = new ArrayList<>();
            order  = ORDER_ASC;
            files = new ArrayList<>();
            json = campaignsJSON;
            for (int i = 0; i < campaignsJSON.length(); i++) {
                JSONObject campaignPlusRoot = campaignsJSON.getJSONObject(i);
                Campaign campaign = new Campaign(campaignPlusRoot.getJSONObject("campaign"),campaignPlusRoot.getString("ROOT"));
                if (campaign.getFiles() != null){
                    files.addAll(campaign.getFiles());
                }
                this.campaigns.add(campaign);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public CampaignMultiple(Parcel in) throws JSONException {
        this(new JSONArray(in.readString()));
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
    }

    @Override
    public void setNextSpecificFileId(int fileId) {
        for (int i = 0; i < campaigns.size(); i++) {
            int position  = campaigns.get(i).getCampaignFilePositionById(fileId);
            if (position != -1){
                campaignPosition = i;
                this.position = position;
            }
        }
        Log.e(TAG, " File with id " + fileId +" not found, everything stays the same");
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
        if (o == null || !o.getClass().equals(getClass())){
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


    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        JSONArray campaignsArray = new JSONArray();
        for (Campaign campaign : campaigns) {
            try {
                JSONObject campaignPlusRoot = new JSONObject();
                campaignPlusRoot.put("ROOT",campaign.getROOTString());
                campaignPlusRoot.put("campaign",campaign.getJson());
                campaignsArray.put(campaignPlusRoot);
            } catch (JSONException e){
                e.printStackTrace();
            }
        }
        parcel.writeString(campaignsArray.toString());
    }


    @Override
    public JSONObject getJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("campaigns",this.json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
