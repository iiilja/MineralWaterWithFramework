package ee.promobox.promoboxandroid;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Viktor on 11/16/2014.
 */
public class CampaignList extends ArrayList<Campaign> {

    public CampaignList(int size) {
        super(size);
    }

    public CampaignList() {
        super();
    }

    public CampaignList(JSONArray campaignsJSONArray) {
        this();
        try {
            int campaignsCount = campaignsJSONArray.length();
            for(int i =0; i < campaignsCount; i++) {
                this.add(new Campaign(campaignsJSONArray.getJSONObject(i)));
            }
        } catch (JSONException je) {
            Log.e("CampaignList", je.getMessage());
        }
    }

    public Campaign getCampaignWithId(int id){
        for (int i = 0; i < size(); i++) {
            Campaign campaign = get(i);
            if (campaign.getCampaignId() == id){
                return campaign;
            }
        }
        return null;
    }

}
