package ee.promobox.promoboxandroid;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
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
import java.util.Enumeration;
import java.util.List;

import ee.promobox.promoboxandroid.util.ToastIntent;

/**
 * Created by Maxim on 15.12.2014.
 */
public class DownloadFilesTask extends AsyncTask<String, Integer, File> {

    private final String DOWNLOAD_FILE_TASK = "DownloadFilesTask ";

    private MainService service;

    public DownloadFilesTask(MainService service) {
        this.service = service;
    }

    protected File doInBackground(String... urls) {

        try {

            CampaignList oldCampaigns = service.getCampaigns();

            JSONObject data = loadData(urls[0]);

            if (service.getIsDownloading().get()) return null;

            if (data != null) {

                Log.d(DOWNLOAD_FILE_TASK, "Data: " + data.toString());

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

                    CampaignList newCampaigns = new CampaignList(data.getJSONArray("campaigns"));

                    service.setOrientation(data.optInt("orientation", MainActivity.ORIENTATION_LANDSCAPE));
                    service.getSharedPref().edit().putInt("orientation", service.getOrientation()).commit();

                    boolean campaignsUpdated = false;

                    if (oldCampaigns == null) { // Campaigns are not yet initialised.
                        service.setCampaigns(newCampaigns);

                        for (Campaign camp : newCampaigns) {
                            downloadFiles(camp);
                        }

                        campaignsUpdated = true;

                    } else { // Have previous campaigns.
                        int oldCampaignsCount = oldCampaigns.size();

                        service.setCampaigns(new CampaignList(newCampaigns.size()));

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
                                service.getCampaigns().add(newCampaign);

                                downloadFiles(newCampaign);

                                campaignsUpdated = true;
                            } else { // Otherwise just add old one.
                                service.getCampaigns().add(oldCampaign);
                            }
                        }
                    }

                    if (campaignsUpdated) {
                        Campaign oldCampaign = service.getCurrentCampaign();

                        service.selectNextCampaign();

                        // NB! Reusing variable to store if we should update current campaign in main activity.
                        // If new campaign was assigned instead of missing one.
                        // If campaign stopped
                        // If campaign simply changed to another one.
                        campaignsUpdated = oldCampaign == null && service.getCurrentCampaign() != null
                                        || oldCampaign != null && service.getCurrentCampaign() == null
                                        || oldCampaign != null//&& service.getCurrentCampaign() != null
                                            && oldCampaign.getCampaignId() != service.getCurrentCampaign().getCampaignId();
                        if (campaignsUpdated) {

                            Intent finish = new Intent(MainActivity.ACTIVITY_FINISH);
                            LocalBroadcastManager.getInstance(service).sendBroadcast(finish);

                            Intent update = new Intent(MainActivity.CAMPAIGN_UPDATE);
                            LocalBroadcastManager.getInstance(service).sendBroadcast(update);

                            Log.i(DOWNLOAD_FILE_TASK, "Send intent about update");

                        }
                    }
                } else {
                    Log.w(DOWNLOAD_FILE_TASK, "Data has no campaigns.");
                }
            } else {
                Log.w(DOWNLOAD_FILE_TASK, "No data.");
            }
        } catch (Exception ex) {
            Log.e(DOWNLOAD_FILE_TASK, ex.getMessage(), ex);
            LocalBroadcastManager.getInstance(service).sendBroadcast(new ToastIntent(DOWNLOAD_FILE_TASK + ex.toString()));
        }

        return null;
    }

    public void downloadFiles(Campaign camp) {

        Log.i("MainService", "Download files");

        LocalBroadcastManager.getInstance(service).sendBroadcast(new ToastIntent("Downloading " + camp.getCampaignName()));

        service.setLoadingCampaign(camp);
        service.setLoadingCampaignProgress(0);

        service.getIsDownloading().set(true);

        List<CampaignFile> campaignFiles = camp.getFiles();
        int loadStep = 100 / campaignFiles.size();

        for (CampaignFile f : campaignFiles) {
            service.setLoadingCampaignProgress(service.getLoadingCampaignProgress() + loadStep);

            File dir = new File(MainService.ROOT.getAbsolutePath() + String.format("/%s/", camp.getCampaignId()));

            dir.mkdirs();

            File file = new File(dir, f.getId() + "");

            if (!file.exists() || file.length() != f.getSize()) {
                if (service.getOrientation() != MainActivity.ORIENTATION_PORTRAIT_EMULATION) {
                    downloadFile(String.format(MainService.DEFAULT_SERVER + "/service/files/%s", f.getId()), f.getId() + "", camp);
                } else {

                    File filePort = new File(dir, f.getId() + "_port");

                    if (!filePort.exists()) {
                        downloadFile(String.format(MainService.DEFAULT_SERVER + "/service/files/%s?orient=3", f.getId()), f.getId() + "_port", camp);
                    }
                }
            }
        }

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
                File dir = new File(MainService.ROOT.getAbsolutePath() + String.format("/%s/", camp.getCampaignId()));

                File file = new File(dir, fileName);

                FileOutputStream f = new FileOutputStream(file);

                InputStream in = entity.getContent();

                IOUtils.copy(in, f);

                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(f);

                Log.i(DOWNLOAD_FILE_TASK, "Size " + file.getAbsolutePath() + " = " + file.length());

                return file;
            }


        } catch (Exception e) {
            Log.d(DOWNLOAD_FILE_TASK, e.getMessage(), e);
            LocalBroadcastManager.getInstance(service).sendBroadcast(new ToastIntent(DOWNLOAD_FILE_TASK + e.toString()));
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

        json.put("cache", dirSize(MainService.ROOT.getAbsoluteFile()));
        json.put("currentFileId", service.getCurrentFileId());
        json.put("currentCampaignId", service.getCurrentCampaign() != null ? service.getCurrentCampaign().getCampaignId() : 0);

        if (service.getLoadingCampaign() != null) {
            json.put("loadingCampaingId", service.getLoadingCampaign().getCampaignId());
            json.put("loadingCampaingProgress", service.getLoadingCampaignProgress());
        }

        Log.i(DOWNLOAD_FILE_TASK, "Pull info:" + json.toString());

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("json", json.toString()));
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        Log.i(DOWNLOAD_FILE_TASK, httppost.getRequestLine().toString());

        HttpResponse response = httpclient.execute(httppost);

        if (response.getStatusLine().getStatusCode() == 200 && !service.getIsDownloading().get()) {
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                MainService.ROOT.mkdirs();

                File file = new File(MainService.ROOT, "data.json");

                FileOutputStream fileOutputStream = new FileOutputStream(file);

                InputStream in = entity.getContent();

                IOUtils.copy(in, fileOutputStream);

                IOUtils.closeQuietly(fileOutputStream);
                IOUtils.closeQuietly(in);

                return new JSONObject(FileUtils.readFileToString(file));
            }
        } else {
            String error = IOUtils.toString(response.getEntity().getContent());
            Log.e(DOWNLOAD_FILE_TASK, error);
            LocalBroadcastManager.getInstance(service).sendBroadcast(new ToastIntent(DOWNLOAD_FILE_TASK + error));
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
            for (int i = 0; i < fileList.length; i++) {
                // Recursive call if it's a directory
                if (fileList[i].isDirectory()) {
                    result += dirSize(fileList[i]);
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
