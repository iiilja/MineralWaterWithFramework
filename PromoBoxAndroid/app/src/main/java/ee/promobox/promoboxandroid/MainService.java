package ee.promobox.promoboxandroid;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;


public class MainService extends Service {

    private final String DEFAULT_SERVER = "http://46.182.31.101:8080"; //"http://api.promobox.ee/";
    private final String DEFAULT_SERVER_JSON = DEFAULT_SERVER + "/service/device/%s/pull";

    private SharedPreferences sharedPref;

    private String uuid;
    private String audioDevice; // which audio interface is used for output
    private int orientation;
    private int currentFileId;
    private int loadingCampaignProgress;
    private boolean alwaysOnTop = false;

    private Campaign campaign; // current campaign.
    private Campaign loadingCampaign;
    private CampaignList campaigns;

    File root = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/promobox/");

    private final IBinder mBinder = new MainServiceBinder();

    private DownloadFilesTask dTask = new DownloadFilesTask();

    @Override
    public void onCreate() {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i("MainService", "Start command");

        setUuid(sharedPref.getString("uuid", "fail"));
        setOrientation(sharedPref.getInt("orientation", MainActivity.ORIENTATION_LANDSCAPE));
        alwaysOnTop = sharedPref.getBoolean("always_on_top", false);

        checkAndDownloadCampaign();

        if (!isActive() && alwaysOnTop) {
            Intent mainActivity = new Intent(getBaseContext(), MainActivity.class);

            mainActivity.setAction(Intent.ACTION_MAIN);
            mainActivity.addCategory(Intent.CATEGORY_LAUNCHER);
            mainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);

            getApplication().startActivity(mainActivity);

        }

        return Service.START_NOT_STICKY;
    }

    public void checkAndDownloadCampaign() {
        try {
            File data = new File(root, "data.json");

            if (data.exists()) {
                String dataString = FileUtils.readFileToString(data);
                Log.d("MainService", "Data from file: " + dataString);
                campaigns = new CampaignList(new JSONObject(dataString).getJSONArray("campaigns"));
                selectNextCampaign();
                //campaign = new Campaign(new JSONObject(FileUtils.readFileToString(data)).getJSONObject("campaign"));
            }
        } catch (Exception ex) {
            Log.e("MainService", ex.getMessage(), ex);
        }

        if (getUuid() != null) {
            dTask = new DownloadFilesTask(dTask.getStatus() == AsyncTask.Status.RUNNING);
            dTask.execute(String.format(DEFAULT_SERVER_JSON, getUuid()));
        }

    }

    public boolean isActive() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);

        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;

        if (componentInfo.getPackageName().startsWith("ee.promobox.promoboxandroid")) {
            return true;
        }

        return false;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    private void selectNextCampaign() {
        if(campaigns != null) {
            Date currentDate = new Date();

            // Check for invalid device date.
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            int year = calendar.get(Calendar.YEAR);
            if(year < 2014) {
                if(campaigns.size() > 0) {
                    campaign = campaigns.get(0);
                    return;
                }
            }

            Log.d("MainService", "Current Date: " + currentDate);
            for(Campaign camp: campaigns) {
                // Current date between start and end dates of campaign.
                Log.d("MainService", "Campaign: " +camp.getCampaignName()+" Start: " + camp.getStartDate().toString() + " End: " + camp.getEndDate().toString());
                if(currentDate.after(camp.getStartDate()) && currentDate.before(camp.getEndDate())) {
                    Log.d("MainService", "Date bounds for campaign: " + camp.getCampaignName());
                    campaign = camp;
                    break;
                } else {
                    Log.d("MainService", "Not in date bounds");
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        sharedPref.edit().putString("uuid", uuid).commit();
        this.uuid = uuid;
    }

    public int getCurrentFileId() {
        return currentFileId;
    }

    public void setCurrentFileId(int currentFileId) {
        this.currentFileId = currentFileId;
    }

    public String getAudioDevice() {
        return audioDevice;
    }

    private void setAudioDevice(String audioDevice) {
        sharedPref.edit().putString(MainActivity.AUDIO_DEVICE_PREF, audioDevice).commit();
        this.audioDevice = audioDevice;
    }

    public class MainServiceBinder extends Binder {
        MainService getService() {
            return MainService.this;
        }
    }

    private class DownloadFilesTask extends AsyncTask<String, Integer, File> {

        private boolean pullOnly;

        public DownloadFilesTask() {
            this(false);
        }

        public DownloadFilesTask(boolean pullOnly) {
            this.pullOnly = pullOnly;
        }

        protected File doInBackground(String... urls) {

            try {
                CampaignList oldCampaigns = campaigns;

                JSONObject data = loadData(urls[0]);
                if(pullOnly) return null;

                if (data != null) {

                    Log.d("MainService", "Data: " + data.toString());

                    if(data.has("audioOut")) {
                        int deviceId = data.getInt("audioOut");
                        String device;
                        switch(deviceId) {
                            case 1:
                                device = "AUDIO_HDMI";
                                break;
                            case 2:
                                device = "AUDIO_SPDIF";
                                break;
                            default:
                            case 0:
                                device = "AUDIO_CODEC";
                                break;
                        }
                        setAudioDevice(device);
                    }

                    if (data.has("campaigns")) {
                        CampaignList newCampaigns = new CampaignList(data.getJSONArray("campaigns"));

                        setOrientation(data.optInt("orientation", MainActivity.ORIENTATION_LANDSCAPE));
                        sharedPref.edit().putInt("orientation", orientation).commit();

                        boolean campaignsUpdated = false;

                        if (oldCampaigns == null) { // Campaigns are not yet initialised.
                            campaigns = newCampaigns;

                            for (Campaign camp : newCampaigns) {
                                downloadFiles(camp);
                            }

                            campaignsUpdated = true;

                        } else { // Have previous campaigns.
                            int oldCampaignsCount = oldCampaigns.size();
                            campaigns = new CampaignList(newCampaigns.size());
                            Campaign oldCampaign;
                            Campaign bufCampaign;
                            int oldCampaignIndex = -1;
                            int newCampaignId;

                            for (Campaign newCampaign : newCampaigns) {

                                newCampaignId = newCampaign.getCampaignId();
                                oldCampaign = null;

                                // Find old campaign with same id.
                                // note: size might change every iteration of newCampaigns.
                                for (int i = 0; i < oldCampaignsCount; i++) {
                                    bufCampaign = oldCampaigns.get(i);

                                    if (newCampaignId == bufCampaign.getCampaignId()) {
                                        oldCampaign = bufCampaign;
                                        oldCampaignIndex = i;
                                        break;
                                    }
                                }
                                // If campaign not in list or needs to be updated add new one.
                                if (oldCampaign == null || (newCampaign.getUpdateDate() > oldCampaign.getUpdateDate())) {
                                    campaigns.add(newCampaign);
                                    downloadFiles(newCampaign);
                                    campaignsUpdated = true;
                                } else { // Otherwise just add old one.
                                    campaigns.add(oldCampaign);
                                }
                            }
                        }

                        if (campaignsUpdated) {
                            Campaign oldCampaign = campaign;
                            selectNextCampaign();

                            // NB! Reusing variable to store if we should update current campaign in main activity.
                            // If new campaign was assigned instead of missing one.
                            campaignsUpdated = oldCampaign == null && campaign != null;
                            // If campaign stopped.
                            if(!campaignsUpdated) campaignsUpdated = oldCampaign != null && campaign == null;
                            // If campaign simply changed to another one.
                            if(!campaignsUpdated) campaignsUpdated = oldCampaign != null && campaign != null && oldCampaign.getCampaignId() != campaign.getCampaignId();

                            if (campaignsUpdated) {
                                Intent finish = new Intent(MainActivity.ACTIVITY_FINISH);
                                LocalBroadcastManager.getInstance(MainService.this).sendBroadcast(finish);
                                Intent update = new Intent(MainActivity.CAMPAIGN_UPDATE);
                                LocalBroadcastManager.getInstance(MainService.this).sendBroadcast(update);
                            }
                        }
                    } else {
                        Log.w("MainService", "Data has no campaigns.");
                    }
                } else {
                    Log.w("MainService", "No data.");
                }
            } catch (Exception ex) {
                Log.e("MainService", ex.getMessage(), ex);
            }

            return null;
        }

        public void downloadFiles(Campaign camp) {

            Log.i("MainService", "Download files");

            loadingCampaign = camp;
            loadingCampaignProgress = 0;

            List<CampaignFile> campaignFiles = camp.getFiles();
            int loadStep = 100 / campaignFiles.size();

            for (CampaignFile f : campaignFiles) {
                loadingCampaignProgress += loadStep;
                File dir = new File(root.getAbsolutePath() + String.format("/%s/", camp.getCampaignId()));

                dir.mkdirs();

                File file = new File(dir, f.getId() + "");

                if (!file.exists() || file.length() != f.getSize()) {
                    if (orientation != MainActivity.ORIENTATION_PORTRAIT_EMULATION) {
                        downloadFile(String.format(DEFAULT_SERVER + "/service/files/%s", f.getId()), f.getId() + "", camp);
                    } else {

                        File filePort = new File(dir, f.getId() + "_port");

                        if (!filePort.exists()) {
                            downloadFile(String.format(DEFAULT_SERVER + "/service/files/%s?orient=3", f.getId()), f.getId() + "_port", camp);
                        }
                    }
                }
            }

            loadingCampaignProgress = 100;
        }

        private File downloadFile(String fileURL, String fileName, Campaign camp) {
            try {

                Log.i("MainService", fileURL);

                HttpClient httpclient = new DefaultHttpClient();

                HttpGet httpget = new HttpGet(fileURL);

                HttpResponse response = httpclient.execute(httpget);

                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    File dir = new File(root.getAbsolutePath() + String.format("/%s/", camp.getCampaignId()));

                    File file = new File(dir, fileName);

                    FileOutputStream f = new FileOutputStream(file);

                    InputStream in = entity.getContent();

                    IOUtils.copy(in, f);

                    IOUtils.closeQuietly(in);
                    IOUtils.closeQuietly(f);

                    Log.i("MainService", "Size " + file.getAbsolutePath() + " = " + file.length());

                    return file;
                }


            } catch (Exception e) {
                Log.d("MainService", e.getMessage(), e);
            }

            return null;

        }

        private JSONObject loadData(String url) throws Exception {

            HttpClient httpclient = new DefaultHttpClient();

            HttpPost httppost = new HttpPost(url);

            JSONObject json = new JSONObject();

            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();


            final Enumeration<NetworkInterface> listNetworks = NetworkInterface.getNetworkInterfaces();

            JSONArray listInterface = new JSONArray();

            while (listNetworks.hasMoreElements()) {
                final NetworkInterface networkInterface = listNetworks.nextElement();

                JSONObject obj = new JSONObject();

                obj.put("name", networkInterface.getDisplayName());

                JSONArray ipArray = new JSONArray();

                Enumeration<InetAddress> listAddresses = networkInterface.getInetAddresses();

                while (listAddresses.hasMoreElements()) {
                    ipArray.put(listAddresses.nextElement().getHostAddress());
                }

                obj.put("ip", ipArray);

                listInterface.put(obj);

            }

            json.put("ip", listInterface);
            json.put("freeSpace", bytesAvailable);
            json.put("force", 1);

            json.put("cache", dirSize(root.getAbsoluteFile()));
            json.put("currentFileId", currentFileId);

            if(loadingCampaign != null) {
                json.put("loadingCampaingId", loadingCampaign.getCampaignId());
                json.put("loadingCampaingProgress", loadingCampaignProgress);
            } else {
                json.put("loadingCampaingProgress", 0);
                json.put("loadingCampaingProgress", 0);
            }
            Log.i("MainService", "Pull info:" + json.toString());

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("json", json.toString()));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            Log.i("MainService", httppost.getRequestLine().toString());

            HttpResponse response = httpclient.execute(httppost);

            if (response.getStatusLine().getStatusCode() == 200 && !pullOnly) {
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    root.mkdirs();

                    File file = new File(root, "data.json");

                    FileOutputStream fileOutputStream = new FileOutputStream(file);

                    InputStream in = entity.getContent();

                    IOUtils.copy(in, fileOutputStream);

                    IOUtils.closeQuietly(fileOutputStream);
                    IOUtils.closeQuietly(in);

                    return new JSONObject(FileUtils.readFileToString(file));
                }
            } else {
                Log.e("MainService", IOUtils.toString(response.getEntity().getContent()));
            }

            return null;
        }


        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(File result) {
        }

        private long dirSize(File dir) {

            if (dir.exists()) {
                long result = 0;
                File[] fileList = dir.listFiles();
                for(int i = 0; i < fileList.length; i++) {
                    // Recursive call if it's a directory
                    if(fileList[i].isDirectory()) {
                        result += dirSize(fileList [i]);
                    } else {
                        // Sum the file size in bytes
                        result += fileList[i].length();
                    }
                }
                return result; // return the file size
            }
            return 0;
        }
    }


}
