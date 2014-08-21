package ee.promobox.promoboxandroid;

import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Date;
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
    private long updateDate;

    private Date startDate;
    private Date endDate;
    private int delay;

    private List<CampaignFile> files = new ArrayList<CampaignFile>();

    public Campaign() {

    }

    public Campaign(JSONObject json) {
        try {

            setClientId(json.getInt("clientId"));
            setCampaignId(json.getInt("campaignId"));
            setCampaignName(json.getString("campaignName"));
            setUpdateDate(json.getLong("updateDate"));
            setDelay(json.getInt("duration"));

            JSONArray ar  = json.getJSONArray("files");

            for (int i=0; i<ar.length(); i++) {
                JSONObject obj = ar.getJSONObject(i);

                CampaignFile f = new CampaignFile();

                f.setId(obj.getInt("id"));
                f.setType(CampaignFileType.valueOf(obj.getInt("type")));
                f.setSize(obj.getInt("size"));

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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
    }
}
