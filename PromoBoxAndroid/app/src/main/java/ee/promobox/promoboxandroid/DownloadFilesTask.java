package ee.promobox.promoboxandroid;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.StatFs;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import ee.promobox.promoboxandroid.data.AudioOut;
import ee.promobox.promoboxandroid.data.Campaign;
import ee.promobox.promoboxandroid.data.CampaignFile;
import ee.promobox.promoboxandroid.data.CampaignList;
import ee.promobox.promoboxandroid.data.Display;
import ee.promobox.promoboxandroid.data.DisplayArrayList;
import ee.promobox.promoboxandroid.data.ErrorMessage;
import ee.promobox.promoboxandroid.data.ErrorMessageArray;
import ee.promobox.promoboxandroid.intents.SetStatusIntent;
import ee.promobox.promoboxandroid.intents.ToastIntent;
import ee.promobox.promoboxandroid.util.InternetConnectionUtil;
import ee.promobox.promoboxandroid.util.StatusEnum;

/**
 * Created by Maxim on 15.12.2014.
 */
public class DownloadFilesTask extends AsyncTask<String, Integer, File> {

    private final String TAG = "DownloadFilesTask ";


    private MainService service;
    private boolean onlySendData = false;


    public DownloadFilesTask(MainService service) {
        this.service = service;
    }

    protected File doInBackground(String... urls) {

        if (!isNetworkConnected()) {
            if ( service.getWifiRestartCounter() > 5 ){
                service.setWifiRestartCounter(0);
                service.sendBroadcast(new ToastIntent("No network"));
            }
            return null;
        }
        try {

            JSONObject data = loadData(urls[0]);
            if (onlySendData || service.getIsDownloading().get() || data == null){
                Log.w(TAG, "Exiting "+ TAG);
                return null;
            }

            Log.d(TAG, "Data: " + data.toString());

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

                service.setAudioDevice(AudioOut.getByOutNumber(deviceId));
            }
            if (data.has("videoWall") && data.getBoolean("videoWall")) {
                configureVideoWall(data);
            }

            if (data.has("campaigns")) {
                service.setOrientation(data.optInt("orientation", MainActivity.ORIENTATION_LANDSCAPE));

                handleCampaigns(data.getJSONArray("campaigns"));
            } else {
                service.setCampaigns(new CampaignList());
                Log.w(TAG, "Data has no campaigns.");
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
                Log.d(TAG, "CLEARING CACHE");
                clearCache();
            }
            if (data.has("openApp") && data.getBoolean("openApp")){
                Log.d(TAG, " Received OPEN APP");
                service.startMainActivity();
            }
            if (data.has("nextFile")){
                int nextFile = data.getInt("nextFile");
                playThisFile(nextFile);
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);
            service.sendBroadcast(new ToastIntent(String.format(MainActivity.ERROR_MESSAGE, 51 , ex.getClass().getSimpleName())));
            service.addError(new ErrorMessage(ex.toString(), ex.getMessage(), ex.getStackTrace()), false);
        }

        return null;
    }

    private void configureVideoWall(JSONObject data) {
        int wallHeight;
        int wallWidth;
        DisplayArrayList displays = new DisplayArrayList();
        try {
            if (data.has("resolutionHorizontal") && data.has("resolutionVertical") && data.has("displays")){
                wallHeight = data.getInt("resolutionVertical");
                wallWidth = data.getInt("resolutionHorizontal");
                JSONArray displaysJSON = data.getJSONArray("displays");
                for (int i = 0; i < displaysJSON.length(); i++) {
                    displays.add(new Display(displaysJSON.getJSONObject(i)));
                }
                boolean updated;
                updated = service.setDisplays(displays);
                updated |= service.setWallHeight(wallHeight);
                updated |= service.setWallWidth(wallWidth);
                updated |= service.setVideoWall(true);
                if (updated){
                    Intent update = new Intent(MainActivity.WALL_UPDATE);
                    service.sendBroadcast(update);
                }
            } else {
                Log.e(TAG, "resolutionHorizontal or resolutionVertical or displays missing in JSON");
            }
        } catch (JSONException e) {
            service.addError(new ErrorMessage(e), false);
            e.printStackTrace();
        }
    }

    private void playThisFile(int nextFile) {
        Log.d(TAG,"playThisFile() file id = " + nextFile);
        CampaignList campaignList = service.getCampaigns();
        CampaignFile campaignFile = campaignList.getCampaignFileByFileId(nextFile);
        if ( campaignFile == null ) {
            service.addError(new ErrorMessage("server error mby", "nextFile = "
                    + nextFile + "while no such file in campaignList", null), false);
            return;
        }
        Intent intent = new Intent(MainActivity.PLAY_SPECIFIC_FILE);
        intent.putExtra("campaignFileId",campaignFile.getId());
        service.sendBroadcast(intent);
    }

    private void downloadFiles(Campaign camp) {

        Log.i("MainService", "Download files");

        service.sendBroadcast(new ToastIntent("Downloading " + camp.getCampaignName()));

        service.setLoadingCampaign(camp);
        service.setLoadingCampaignProgress(0);

        service.getIsDownloading().set(true);

        Campaign serviceCampaign = service.getCampaigns() != null ? service.getCampaigns().getCampaignWithId(camp.getCampaignId()): null;

        List<CampaignFile> campaignFiles = camp.getFiles();
        double loadStep = 100d / (campaignFiles.size()!= 0 ? campaignFiles.size() : 1);

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
            Log.d(TAG, "check size real file = " +  file.length() + " campaign file = " + f.getSize());
            boolean filesDifferent  = serviceCampaignFile != null
                    && (serviceCampaignFile.getUpdatedDt() < f.getUpdatedDt()
                        || serviceCampaignFile.getUpdatedDt() == 0 && f.getUpdatedDt() == 0 &&
                            file.length() != f.getSize())
                    || file.length() != f.getSize()
                        /*&& serviceCampaignFile == null */ ;
                        // TODO: Make something in case if network was lost during downloading file



            if (filesDifferent) {

                service.sendBroadcast(new SetStatusIntent( StatusEnum.DOWNLOADING,
                        "Downloading " + camp.getCampaignName() + " files "+ (i+1) + "/" + campaignFiles.size()));

                Log.d(TAG, "CampaignFIle "+f.getId()+" f.getSize() = " + f.getSize()
                        + " real FILE length = " +  file.length()+" getTotalSpace = " +  file.getTotalSpace() + " and directory :" + file.getAbsolutePath() );
                boolean downloaded = downloadFile(String.format(MainService.DEFAULT_SERVER + "/service/files/%s", f.getId()), f.getId() + "", camp);
                if (!downloaded){
                    campaignFiles.remove(i);
                }
            }
        }
        service.sendBroadcast(new SetStatusIntent(StatusEnum.DOWNLOADED,""));
        service.setLoadingCampaignProgress(100);
        service.setLoadingCampaign(null);
        service.getIsDownloading().set(false);
    }

    private boolean downloadFile(String fileURL, String fileName, Campaign camp) {
        try {

            Log.i(TAG, fileURL);

            URL url = new URL(fileURL);
            URLConnection urlConnection = url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            File dir = new File(service.getROOT().getAbsolutePath() + String.format("/%s/", camp.getCampaignId()));

            File file = new File(dir, fileName);

            FileOutputStream f = new FileOutputStream(file);

            try {
                IOUtils.copy(in, f);
            } finally{
                IOUtils.closeQuietly(in);
            }

            IOUtils.closeQuietly(f);

            Log.i(TAG, "Size " + file.getAbsolutePath() + " = " + file.length());

            return true;



        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);
            service.sendBroadcast(new ToastIntent(String.format(MainActivity.ERROR_MESSAGE, 52 , ex.getClass().getSimpleName())));
            service.addError(new ErrorMessage(ex.toString(), ex.getMessage(), ex.getStackTrace()), false);
        }
        return false;
    }

    private JSONObject loadData(String urlString) throws Exception {

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        conn.setReadTimeout(10000);
//        conn.setConnectTimeout(15000);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
//        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        conn.setDoOutput(true);

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
        json.put("version", BuildConfig.VERSION_CODE);

        service.setErrors(new ErrorMessageArray());

        if (service.getLoadingCampaign() != null) {
            json.put("loadingCampaignId", service.getLoadingCampaign().getCampaignId());
            json.put("loadingCampaignProgress", (int) service.getLoadingCampaignProgress());
        }


        Log.i(TAG, "Pull info:" + json.toString());
        Log.i(TAG, conn.getURL().toString());


        String query = "json=" + URLEncoder.encode(json.toString(), "UTF-8");
        IOUtils.write(query.getBytes(),conn.getOutputStream());
        String response = "";

        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK && !service.getIsDownloading().get()) {
            Log.d(TAG, "Response code = " + conn.getResponseCode());
            response = IOUtils.toString(conn.getInputStream(),"UTF-8");
            if (!response.equals("")) {
                try {
                    return new JSONObject(response);
                } catch (JSONException e) {
                    ErrorMessage message = new ErrorMessage("JSONException", e.getMessage(), e.getStackTrace());
                    message.putMoreInfo("json:" + response);
                    service.addError(message, false);
                    service.sendBroadcast(new ToastIntent("Error 53 - reading JSON from server"));
                }
            }
        } else if ( !service.getIsDownloading().get() ){
            String error = " Status code:" + conn.getResponseCode();
            String content = IOUtils.toString(conn.getErrorStream(), "UTF-8");
            error += ". Content:" + content ;
            Log.e(TAG, error);
            json = null;
            try {
                json = new JSONObject(content);
            } catch (JSONException e){
                service.addError(new ErrorMessage("ServerError", error, null), false);
                service.sendBroadcast(new ToastIntent("Error reading JSON from server"));
            }
            if ( json != null && json.has("error") && json.getString("error").equals("not_found_device")) {
                Log.d(TAG, "sending WRONG_UUID" );
                service.sendBroadcast(new Intent(MainActivity.WRONG_UUID));
                service.sendBroadcast(new ToastIntent("Wrong UUID"));
            }
        } else {
            Log.d(TAG, "AM BUSY, AM DOWNLOADING");
        }

        return null;
    }


    protected void onProgressUpdate(Integer... progress) {

    }

    protected void onPostExecute(File result) {
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) service.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean result = InternetConnectionUtil.isNetworkConnected(cm);
        if (!result ) {
            Log.d(TAG, "Network not connected");
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE ,-5);
            Date lastWifiRestartDt = service.getLastWifiRestartDt();
            if ( lastWifiRestartDt == null) {
                service.setLastWifiRestartDt(service.getCurrentDate());
            } else if ( lastWifiRestartDt.before(calendar.getTime())){
                service.setLastWifiRestartDt(service.getCurrentDate());
                service.setWifiRestartCounter(service.getWifiRestartCounter() + 1 );
                WifiManager wifiManager = (WifiManager) service.getSystemService(Context.WIFI_SERVICE);
                if (wifiManager.isWifiEnabled()){
                    Log.d(TAG, "Restarting WiFi");
                    wifiManager.setWifiEnabled(false);
                    wifiManager.setWifiEnabled(true);
                }
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
                if (folder.getName().equals("data.json")) {
                    continue;
                }
                int id = Integer.parseInt(folder.getName());
                Log.d(TAG,"Am in folder " + id);
                Campaign campaign = campaignList != null ? campaignList.getCampaignWithId(id) : null;
                if (campaign == null){
                    FileUtils.deleteDirectory(folder);
                    Log.d(TAG,"Removing folder " + id);
                }
                else {
                    for (File file: folder.listFiles()){
                        String fileName = file.getName();
                        if (!campaign.containsFile(fileName)){
                            FileUtils.deleteQuietly(file);
                            Log.d(TAG,"Removing file \t" + fileName);
                        }
                        else {
                            Log.d(TAG,"File is needed \t" + fileName);
                        }
                    }
                }
            } catch (NumberFormatException ex){
                service.sendBroadcast(new ToastIntent("Wrong folder name on cleaning cache"));
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
            service.selectNextCampaign();
        }
    }

    public void setOnlySendData(boolean onlySendData) {
        this.onlySendData = onlySendData;
    }
}
