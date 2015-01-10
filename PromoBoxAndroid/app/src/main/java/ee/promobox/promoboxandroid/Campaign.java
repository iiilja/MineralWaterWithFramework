package ee.promobox.promoboxandroid;

import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;

/**
 * Created by MaximDorofeev on 12.07.2014.
 */
public class Campaign {

    public final static int ORDER_ASC = 1;
    public final static int ORDER_RANDOM = 2;

    private int clientId;
    private int campaignId;
    private int updateId;
    private String campaignName;
    private long updateDate;

    private Date startDate;
    private Date endDate;
    private int delay;
    private int sequence;

    private ArrayList<Integer> days = new ArrayList<Integer>();
    private ArrayList<Integer> hours = new ArrayList<Integer>();

    private final String[] serverDaysOfWeek = {"su","mo","tu","we","th","fr","sa"};

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
            setSequence(json.getInt("sequence"));
            setStartDate(new Date(json.getLong("startDate")));
            setEndDate(new Date(json.getLong("endDate")));
            setDays(json.getJSONArray("days"));
            setHours(json.getJSONArray("hours"));
            JSONArray ar = json.getJSONArray("files");

            for (int i = 0; i < ar.length(); i++) {
                JSONObject obj = ar.getJSONObject(i);

                CampaignFile f = new CampaignFile();

                f.setId(obj.getInt("id"));
                f.setType(CampaignFileType.valueOf(obj.getInt("type")));
                f.setSize(obj.getInt("size"));
                f.setPath(new File(getRoot(), f.getId() + "").getAbsolutePath());

                files.add(f);

            }

            Collections.shuffle(files);

            if (getSequence() == ORDER_ASC) {
                Collections.sort(files);
            }


        } catch (Exception ex) {
            Log.e("Campaign", ex.getMessage(), ex);
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

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public void setHours(JSONArray hoursJSON) throws JSONException {
        for (int i = 0; i < hoursJSON.length(); i++){
            hours.add(hoursJSON.getInt(i));
        }
    }

    public void setDays(JSONArray daysJSON) throws Exception {
        for (int i = 0; i < daysJSON.length(); i++){
            days.add(getCalendarDayOfWeek(daysJSON.getString(i)));
        }
    }

    public boolean hasToBePlayed(){
        Calendar cal = Calendar.getInstance();
        Date currentDate = cal.getTime();
        if(currentDate.after(getStartDate()) && currentDate.before(getEndDate())) {
            Integer dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            Integer hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
//            Log.d("Campaign","Between dates");
//            Log.d("Campaign","dayOfWeek = " + dayOfWeek +", hourOfDay = " + hourOfDay);
//            Log.d("Campaign","days.contains(dayOfWeek) = " + days.contains(dayOfWeek));
//            Log.d("Campaign","hours.contains(hourOfDay) = " + hours.contains(hourOfDay));
            if(days.contains(dayOfWeek) && hours.contains(hourOfDay)){
                return true;
            }
        }
        return false;
    }

    private Integer getCalendarDayOfWeek(String day) throws Exception {
        for (int i = 0; i < serverDaysOfWeek.length; i++) {
            if (day.equals(serverDaysOfWeek[i])) {
                return i+1;
            }
        }
        throw new Exception("Day " + day + " not in days list");
    }
}
