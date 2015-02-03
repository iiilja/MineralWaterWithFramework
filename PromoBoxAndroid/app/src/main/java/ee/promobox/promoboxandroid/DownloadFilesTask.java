package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.StatFs;
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
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import ee.promobox.promoboxandroid.data.Campaign;
import ee.promobox.promoboxandroid.data.CampaignFile;
import ee.promobox.promoboxandroid.data.CampaignList;
import ee.promobox.promoboxandroid.data.ErrorMessage;
import ee.promobox.promoboxandroid.data.ErrorMessageArray;
import ee.promobox.promoboxandroid.intents.SetStatusIntent;
import ee.promobox.promoboxandroid.intents.ToastIntent;

/**
 * Created by Maxim on 15.12.2014.
 */
public class DownloadFilesTask extends AsyncTask<String, Integer, File> {

    private final String DOWNLOAD_FILE_TASK = "DownloadFilesTask ";

    LocalBroadcastManager bManager;

    private MainService service;
    private boolean onlySendData = false;


    public DownloadFilesTask(MainService service) {
        this.service = service;
        bManager = LocalBroadcastManager.getInstance(service);
    }

    protected File doInBackground(String... urls) {

        if (!isNetworkConnected()) {
            if ( service.getWifiRestartCounter() > 5 ){
                service.setWifiRestartCounter(0);
                bManager.sendBroadcast(new ToastIntent("No network"));
            }
            return null;
        }
        try {

            JSONObject data = loadData(urls[0]);
            if (onlySendData || service.getIsDownloading().get() || data == null){
                Log.w(DOWNLOAD_FILE_TASK, "Exiting "+ DOWNLOAD_FILE_TASK);
                return null;
            }

            Log.d(DOWNLOAD_FILE_TASK, "Data: " + data.toString());

            if (data.has("currentDt")){
                Date serverDate = new Date(data.getLong("currentDt"));
                Date serviceDate = service.getCurrentDate();
                Date before;
                Date after;
                Calendar cal = Calendar.getInstance();
                cal.setTime(serverDate);
                cal.add(Calendar.HOUR_OF_DAY, -8);
                before = cal.getTime();
                cal.setTime(serverDate);
                cal.add(Calendar.HOUR_OF_DAY, 8);
                after = cal.getTime();
                if (serviceDate.before(before) || serviceDate.after(after)) {
                    service.setCurrentDate(serverDate);
                }
            }
            if (data.has("audioOut")) {
                int deviceId = data.getInt("audioOut");

                String device;

                switch (deviceId) {
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

                service.setAudioDevice(device);
            }

            if (data.has("campaigns")) {
                service.setOrientation(data.optInt("orientation", MainActivity.ORIENTATION_LANDSCAPE));
                service.getSharedPref().edit().putInt("orientation", service.getOrientation()).commit();

                handleCampaigns(data.getJSONArray("campaigns"));
            } else {
                service.setCampaigns(new CampaignList());
                Log.w(DOWNLOAD_FILE_TASK, "Data has no campaigns.");
            }
            /*
            Campigns are loaded to device and are set to campaigns in service.
            Device is playing new campaign if was updated.

            Now, when campains are updated and new are set playing we may clear cache.
            And then try play next file
             */

            File jsonDataFile = new File(service.getROOT(), "data.json");
            FileUtils.writeStringToFile(jsonDataFile, data.toString(), "UTF-8");

            if (data.has("clearCache") && data.getBoolean("clearCache")){
                clearCache();
            }
            if (data.has("openApp") && data.getBoolean("openApp")){
                Log.d(DOWNLOAD_FILE_TASK, " Received OPEN APP");
                service.startMainActivity();
            }
            if (data.has("nextFile")){
                int nextFile = data.getInt("nextFile");
                playThisFile(nextFile);
            }
        } catch (Exception ex) {
            Log.e(DOWNLOAD_FILE_TASK, ex.getMessage(), ex);
            bManager.sendBroadcast(new ToastIntent(String.format(MainActivity.ERROR_MESSAGE, 51 , ex.getClass().getSimpleName())));
            service.addError(new ErrorMessage(ex.toString(), ex.getMessage(), ex.getStackTrace()), false);
        }

        return null;
    }

    private void playThisFile(int nextFile) {
        Log.d(DOWNLOAD_FILE_TASK,"playThisFile() file id = " + nextFile);
        CampaignList campaignList = service.getCampaigns();
        CampaignFile campaignFile = campaignList.getCampaignFileByFileId(nextFile);
        Intent intent = new Intent(MainActivity.PLAY_SPECIFIC_FILE);
        intent.putExtra("campaignFile",campaignFile);
        bManager.sendBroadcast(intent);
    }

    private void downloadFiles(Campaign camp) {

        Log.i("MainService", "Download files");

        bManager.sendBroadcast(new ToastIntent("Downloading " + camp.getCampaignName()));

        service.setLoadingCampaign(camp);
        service.setLoadingCampaignProgress(0);

        service.getIsDownloading().set(true);

        Campaign serviceCampaign = service.getCampaigns() != null ? service.getCampaigns().getCampaignWithId(camp.getCampaignId()): null;

        List<CampaignFile> campaignFiles = camp.getFiles();
        double loadStep = 100 / (campaignFiles.size()!= 0 ? campaignFiles.size() : 1);

        for (int i = 0; i < campaignFiles.size(); i++) {
            CampaignFile f  = campaignFiles.get(i);
            service.setLoadingCampaignProgress(service.getLoadingCampaignProgress() + loadStep);

            File dir = new File(service.getROOT().getAbsolutePath() + String.format("/%s/", camp.getCampaignId()));
            CampaignFile serviceCampaignFile = serviceCampaign != null ? serviceCampaign.getFileById(f.getId()) : null;
            File file = new File(dir, f.getId() + "");
            dir.mkdirs();

            /*  If there is campaignFile in service then check updated date.
                    If updated date was not sent in JSON, check file sizes.
                If there is no campaignFile in service, only file in cache,
                    we can only check if file sizes are equal
            */
            Log.d(DOWNLOAD_FILE_TASK, "check size real file = " +  file.length() + " campaign file = " + f.getSize());
            boolean filesDifferent  = serviceCampaignFile != null
                    && (serviceCampaignFile.getUpdatedDt() < f.getUpdatedDt()
                        || serviceCampaignFile.getUpdatedDt() == 0 && f.getUpdatedDt() == 0 &&
                            file.length() != f.getSize())
                    || serviceCampaignFile == null && file.length() != f.getSize();



            if (filesDifferent) {

                bManager.sendBroadcast(new SetStatusIntent(
                        "Downloading " + camp.getCampaignName() + " files "+ (i+1) + "/" + campaignFiles.size()));

                Log.d(DOWNLOAD_FILE_TASK, "CampaignFIle "+f.getId()+" f.getSize() = " + f.getSize()
                        + " real FILE length = " +  file.length()+" getTotalSpace = " +  file.getTotalSpace() + " and directory :" + file.getAbsolutePath() );
                downloadFile(String.format(MainService.DEFAULT_SERVER + "/service/files/%s", f.getId()), f.getId() + "", camp);
            }
        }
        bManager.sendBroadcast(new SetStatusIntent(""));
        service.setLoadingCampaignProgress(100);
        service.setLoadingCampaign(null);
        service.getIsDownloading().set(false);
    }

    private File downloadFile(String fileURL, String fileName, Campaign camp) {
        try {

            Log.i(DOWNLOAD_FILE_TASK, fileURL);

            HttpClient httpclient = new DefaultHttpClient();

            HttpGet httpget = new HttpGet(fileURL);

            HttpResponse response = httpclient.execute(httpget);

            HttpEntity entity = response.getEntity();

            if (entity != null) {
                File dir = new File(service.getROOT().getAbsolutePath() + String.format("/%s/", camp.getCampaignId()));

                File file = new File(dir, fileName);

                FileOutputStream f = new FileOutputStream(file);

                InputStream in = entity.getContent();

                IOUtils.copy(in, f);

                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(f);

                Log.i(DOWNLOAD_FILE_TASK, "Size " + file.getAbsolutePath() + " = " + file.length());

                return file;
            }


        } catch (Exception ex) {
            Log.d(DOWNLOAD_FILE_TASK, ex.getMessage(), ex);
            bManager.sendBroadcast(new ToastIntent(String.format(MainActivity.ERROR_MESSAGE, 52 , ex.getClass().getSimpleName())));
            service.addError(new ErrorMessage(ex.toString(), ex.getMessage(), ex.getStackTrace()), false);
        }

        return null;

    }

    private JSONObject loadData(String url) throws Exception {

        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);

        JSONObject json = new JSONObject();

        StatFs stat = new StatFs(service.getROOTPath());
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
        ComponentName onTop = service.getOnTopComponentInfo();
        json.put("isOnTop", onTop.getPackageName().startsWith(service.getPackageName()));
        json.put("onTopActivity", onTop.getClassName());
        json.put("ip", listInterface);
        json.put("freeSpace", bytesAvailable);
        json.put("force", 1);
        json.put("cache", FileUtils.sizeOfDirectory(service.getROOT().getAbsoluteFile()));
        json.put("currentFileId", service.getCurrentFileId());
        json.put("currentCampaignId", service.getCurrentCampaign() != null ? service.getCurrentCampaign().getCampaignId() : 0);
        json.put("errors", service.getErrors());

        service.setErrors(new ErrorMessageArray());

        if (service.getLoadingCampaign() != null) {
            json.put("loadingCampaingId", service.getLoadingCampaign().getCampaignId());
            json.put("loadingCampaingProgress", (int) service.getLoadingCampaignProgress());
        }


        Log.i(DOWNLOAD_FILE_TASK, "Pull info:" + json.toString());

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("json", json.toString()));
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        Log.i(DOWNLOAD_FILE_TASK, httppost.getRequestLine().toString());
        HttpResponse response = null;
        try{
            response = httpclient.execute(httppost);
        } catch (HttpHostConnectException ex){
            bManager.sendBroadcast(new ToastIntent("Internet connection problem (HttpHostConnectException)"));
            service.addError(new ErrorMessage(ex.toString(), ex.getMessage(), ex.getStackTrace()), false);
            return null;
        } catch (SocketException ex){
            bManager.sendBroadcast(new ToastIntent("Internet connection problem (SocketException)"));
            service.addError(new ErrorMessage(ex.toString(), ex.getMessage(), ex.getStackTrace()), false);
            return null;
        }

        if (response.getStatusLine().getStatusCode() == 200 && !service.getIsDownloading().get()) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {

                String jsonString = IOUtils.toString(response.getEntity().getContent());
                return new JSONObject(jsonString);
            }
        } else if ( !service.getIsDownloading().get() ){
            String error = " Status code:" + response.getStatusLine().getStatusCode();
            String content = IOUtils.toString(response.getEntity().getContent());
            error += ". Content:" + content;
            Log.e(DOWNLOAD_FILE_TASK, error);
            json = null;
            try {
                json = new JSONObject(content);
            } catch (JSONException e){
                service.addError(new ErrorMessage("ServerError", error, null), false);
                bManager.sendBroadcast(new ToastIntent("Error reading JSON from server"));
            }
            if ( json != null && json.has("error") && json.getString("error").equals("not_found_device")) {
                bManager.sendBroadcast(new Intent(MainActivity.WRONG_UUID));
                bManager.sendBroadcast(new ToastIntent("Wrong UUID"));
            }
        } else {
            Log.d(DOWNLOAD_FILE_TASK, "AM BUSY, AM DOWNLOADING");
        }

        return null;
    }


    protected void onProgressUpdate(Integer... progress) {

    }

    protected void onPostExecute(File result) {
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) service.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        boolean result = true;
        if (ni == null) {
            // There are no active networks.
            result = false;
        }
        try {
            InetAddress ipAddress = InetAddress.getByName("google.com");

            if (ipAddress.equals("")) {
                result = false;
            }

        } catch (Exception e) {
            result = false;
        }
        if (!result ) {
            Log.d(DOWNLOAD_FILE_TASK, "Network not connected");
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE ,-5);
            Date lastWifiRestartDt = service.getLastWifiRestartDt();
            if ( lastWifiRestartDt == null || lastWifiRestartDt.before(calendar.getTime())){
                Log.d(DOWNLOAD_FILE_TASK, "Restarting WiFi");
                service.setLastWifiRestartDt(service.getCurrentDate());
                service.setWifiRestartCounter(service.getWifiRestartCounter() + 1 );
                WifiManager wifiManager = (WifiManager) service.getSystemService(Context.WIFI_SERVICE);
                if (wifiManager.isWifiEnabled()){
                    wifiManager.setWifiEnabled(false);
                }
                wifiManager.setWifiEnabled(true);
            }
        }
        else {
            service.setWifiRestartCounter(0);
        }
        return result;
    }

    private void clearCache() throws IOException {
        CampaignList campaignList = service.getCampaigns();
        for (File folder : service.getROOT().listFiles()){
            try{
                int id = Integer.parseInt(folder.getName());
                Log.d(DOWNLOAD_FILE_TASK,"Am in folder " + id);
                Campaign campaign = campaignList != null ? campaignList.getCampaignWithId(id) : null;
                if (campaign == null){
                    FileUtils.deleteDirectory(folder);
                    Log.d(DOWNLOAD_FILE_TASK,"Removing folder " + id);
                }
                else {
                    for (File file: folder.listFiles()){
                        String fileName = file.getName();
                        if (!campaign.containsFile(fileName)){
                            FileUtils.deleteQuietly(file);
                            Log.d(DOWNLOAD_FILE_TASK,"Removing file \t" + fileName);
                        }
                        else {
                            Log.d(DOWNLOAD_FILE_TASK,"File is needed \t" + fileName);
                        }
                    }
                }
            } catch (NumberFormatException ex){
                bManager.sendBroadcast(new ToastIntent("Wrong folder name on cleaning cache"));
                service.addError(new ErrorMessage(ex.toString(), ex.getMessage(), ex.getStackTrace()), false);
            }
        }
    }

    private void handleCampaigns(JSONArray campaigns){

        CampaignList oldCampaigns = service.getCampaigns();
        CampaignList newCampaigns = new CampaignList(campaigns, service.getROOTPath());
        CampaignList campaignsToBeSet = new CampaignList(newCampaigns.size());

        boolean campaignsUpdated = false;

        if (oldCampaigns == null) { // Campaigns are not yet initialised.

            for (Campaign camp : newCampaigns) {
                downloadFiles(camp);
            }
            service.setCampaigns(newCampaigns);

            campaignsUpdated = true;

        } else { // Have previous campaigns.

//            service.setCampaigns(new CampaignList(newCampaigns.size()));

            Campaign oldCampaign;

            int oldCampaignIndex = -1;
            int newCampaignId;

            for (Campaign newCampaign : newCampaigns) {

                newCampaignId = newCampaign.getCampaignId();

                // Find old campaign with same id.
                // note: size might change every iteration of newCampaigns.
                oldCampaign = oldCampaigns.getCampaignWithId(newCampaignId);

                // If campaign not in list or needs to be updated add new one.
                if (oldCampaign == null || (newCampaign.getUpdateDate() > oldCampaign.getUpdateDate())) {
                    campaignsToBeSet.add(newCampaign);

                    downloadFiles(newCampaign);

                    campaignsUpdated = true;
                }
                else { // Otherwise just add old one.
                    campaignsToBeSet.add(oldCampaign);
                }
            }
            service.setCampaigns(campaignsToBeSet);
        }

        if (campaignsUpdated) {
            Campaign oldCampaign = service.getCurrentCampaign();

            service.selectNextCampaign();

//            // NB! Reusing variable to store if we should update current campaign in main activity.
//            // If new campaign was assigned instead of missing one.
//            // If campaign stopped
//            // If campaign simply changed to another one.
//            campaignsUpdated = oldCampaign == null && service.getCurrentCampaign() != null
//                    || oldCampaign != null && service.getCurrentCampaign() == null
//                    || oldCampaign != null//&& service.getCurrentCampaign() != null
//                    && oldCampaign.getCampaignId() != service.getCurrentCampaign().getCampaignId();
//            if (campaignsUpdated) {
//
//                Intent update = new Intent(MainActivity.CAMPAIGN_UPDATE);
//                bManager.sendBroadcast(update);
//
//                Log.i(DOWNLOAD_FILE_TASK, "Send intent about update");
//
//            }
        }
    }

    public void setOnlySendData(boolean onlySendData) {
        this.onlySendData = onlySendData;
    }
}
