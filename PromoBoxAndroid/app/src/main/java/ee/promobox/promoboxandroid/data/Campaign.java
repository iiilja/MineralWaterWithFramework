package ee.promobox.promoboxandroid.data;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by MaximDorofeev on 12.07.2014.
 */
public class Campaign {

    public final static int ORDER_ASC = 1;
    public final static int ORDER_RANDOM = 2;

    private int clientId;
    private int campaignId;
    private String campaignName;
    private long updateDate;

    private Date startDate;
    private Date endDate;
    protected int delay;
    protected int order;

    protected int position;

    private String ROOT = "";

    private ArrayList<Integer> days = new ArrayList<>();
    private ArrayList<Integer> hours = new ArrayList<>();

    private final String[] serverDaysOfWeek = {"su","mo","tu","we","th","fr","sa"};

    protected List<CampaignFile> files = new ArrayList<>();

    public Campaign() {

    }

    public Campaign(JSONObject json, String ROOT) {
        try {
            this.ROOT  = ROOT;

            clientId = json.getInt("clientId");
            campaignId = json.getInt("campaignId");
            campaignName = json.getString("campaignName");
            updateDate = json.getLong("updateDate");
            delay = json.getInt("duration");
            order = json.getInt("sequence");
            startDate = new Date(json.getLong("startDate"));
            endDate = new Date(json.getLong("endDate"));
            setDays(json.getJSONArray("days"));
            setHours(json.getJSONArray("hours"));

            JSONArray ar = json.has("files") ? json.getJSONArray("files"): new JSONArray();

            for (int i = 0; i < ar.length(); i++) {
                JSONObject obj = ar.getJSONObject(i);

                CampaignFile f = new CampaignFile();

                f.setId(obj.getInt("id"));
                f.setType(CampaignFileType.valueOf(obj.getInt("type")));
                f.setOrderId(obj.has("orderId") ? obj.getInt("orderId") : i);
                f.setPath(new File(getRoot(), f.getId() + "").getAbsolutePath());
                f.setSize(obj.getInt("size"));
                f.setUpdatedDt(obj.has("updatedDt")? obj.getLong("updatedDt"):0);
                f.setName(obj.has("name") ? obj.getString("name") : "not named file");

                files.add(f);

            }

            Collections.shuffle(files);

            if (order == ORDER_ASC) {
                Collections.sort(files);
            }


        } catch (Exception ex) {
            Log.e("Campaign", ex.getMessage(), ex);
        }

    }

    public File getRoot() {
        return new File(ROOT + "/" + campaignId + "/");
    }

    public int getCampaignId() {
        return campaignId;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public List<CampaignFile> getFiles() {
        return files;
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

    private void setHours(JSONArray hoursJSON) throws JSONException {
        for (int i = 0; i < hoursJSON.length(); i++){
            hours.add(hoursJSON.getInt(i));
        }
    }

    private void setDays(JSONArray daysJSON) throws Exception {
        for (int i = 0; i < daysJSON.length(); i++){
            days.add(getCalendarDayOfWeek(daysJSON.getString(i)));
        }
    }

    public boolean hasToBePlayed(Date currentDate){
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        if(currentDate.after(startDate) && currentDate.before(endDate)) {
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

    public boolean containsFile(String fileName){
        int fileId;
        try{
            fileId  = Integer.parseInt(fileName);
        }
        catch (NumberFormatException ex){
            return false;
        }
        for (CampaignFile file : files) {
            if (file.getId() == fileId){
                return true;
            }
        }
        return false;
    }

    public CampaignFile getFileById(int fileId){
        for (int i = 0; i < files.size(); i++) {
            CampaignFile file = files.get(i);
            if (file.getId() == fileId){
                return file;
            }
        }
        return null;
    }

    public int getCampaignFilePositionById(int fileId){
        for (int i = 0; i < files.size(); i++) {
            CampaignFile file = files.get(i);
            if (file.getId() == fileId){
                return i;
            }
        }
        return 0;
    }

    public CampaignFile getNextFile(){
        if (files == null || files.size() == 0 ) return null;
        return files.get(position);
    }

    public void setNextFilePosition(){
        if (files.size() == 0){
            position = 0;
            return;
        }
        position ++;
        if (position >= files.size() ) {
            position = 0;
            if (order == ORDER_RANDOM){
                Collections.shuffle(files);
            }
        }
    }

    public void setPreviousFilePosition(){
        if (files == null ){
            position = 0;
            return;
        }
        position = position - 2;
        if (position < 0 ) {
            position = files.size() + position;
            if (position < 0) {
                position = 0;
            }
        }
    }

}
