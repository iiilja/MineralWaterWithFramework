package ee.promobox.promoboxandroid;

import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by MaximDorofeev on 12.07.2014.
 */
public class Campaign {
    private int clientId;
    private int campaignId;
    private int updateId;
    private String campaignName;
    private List<CampaignFile> files = new ArrayList<CampaignFile>();

    public Campaign() {

    }



    public Campaign(JSONObject json) {
        try {
            setClientId(json.getInt("clientId"));
            setCampaignId(json.getInt("campaignId"));
            setUpdateId(json.getInt("updateId"));
            setCampaignName(json.getString("campaignName"));

            JSONArray ar  = json.getJSONArray("files");

            for (int i=0; i<ar.length(); i++) {
                JSONObject obj = ar.getJSONObject(i);

                CampaignFile f = new CampaignFile();
                f.setName(obj.getString("path"));
                f.setType(CampaignFileType.valueOf(obj.getInt("type")));

                files.add(f);

            }

        } catch (Exception ex) {
            Log.e("Campgaign", ex.getMessage(), ex);
        }

    }

    public File getRoot() {
        return new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/promobox/" + campaignId + "/");
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public int getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(int campaignId) {
        this.campaignId = campaignId;
    }

    public int getUpdateId() {
        return updateId;
    }

    public void setUpdateId(int updateId) {
        this.updateId = updateId;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }

    public List<CampaignFile> getFiles() {
        return files;
    }

    public void setFiles(List<CampaignFile> files) {
        this.files = files;
    }
}
