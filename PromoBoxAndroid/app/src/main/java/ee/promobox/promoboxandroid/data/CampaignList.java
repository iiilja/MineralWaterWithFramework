package ee.promobox.promoboxandroid.data;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

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

    public CampaignList(JSONArray campaignsJSONArray, String ROOT) {
        this();
        try {
            int campaignsCount = campaignsJSONArray.length();
            for(int i =0; i < campaignsCount; i++) {
                this.add(new Campaign(campaignsJSONArray.getJSONObject(i), ROOT));
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

    public CampaignFile getCampaignFileByFileId(int fileId){
        for (int i = 0; i < size(); i++) {
            List<CampaignFile> campaignFiles = get(i).getFiles();
            for (int j = 0; j < campaignFiles.size(); j++) {
                CampaignFile campaignFile = campaignFiles.get(j);
                if (campaignFile.getId() == fileId){
                    return campaignFile;
                }
            }
        }
        return null;
    }

}
