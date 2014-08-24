package ee.promobox.promoboxandroid;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
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
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class MainService extends Service {


    private final String DEFAULT_SERVER = "http://api.dev.promobox.ee/";
    private final String DEFAULT_SERVER_JSON = DEFAULT_SERVER + "/service/device/%s/pull";

    private JSONObject settings;
    private String uuid;
    private int orientation;

    File root = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/promobox/");

    private final IBinder mBinder = new MainServiceBinder();

    private Campaign campaign;

    private DownloadFilesTask dTask = new DownloadFilesTask();

    @Override
    public void onCreate() {
        if (getSettings() == null) {
            try {
                settings = loadSettings();

                if (settings != null) {

                    setUuid(settings.getString("deviceUuid"));
                    orientation = settings.optInt("orientation", MainActivity.ORIENTATION_LANDSCAPE);

                    Log.i(this.getClass().getName(), "Device id: " + getUuid());
                }
            } catch (Exception ex) {
                Log.e(this.getClass().getName(), ex.getMessage(), ex);
            }

        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i("MainService","Start command");

        checkAndDownloadCampaign();

        if (!isActive() && false) {
            Intent mainActivity = new Intent(getBaseContext(), MainActivity.class);

            mainActivity.setAction(Intent.ACTION_MAIN);
            mainActivity.addCategory(Intent.CATEGORY_LAUNCHER);
            mainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);

            getApplication().startActivity(mainActivity);

        }

        return Service.START_NOT_STICKY;
    }

    public JSONObject loadSettings() throws Exception {
        File settings = new File(root, "settings.json");

        JSONObject obj = null;

        if (settings.exists()) {
            obj = new JSONObject(FileUtils.readFileToString(settings));
        }

        return obj;
    }

    public void saveSettings(JSONObject obj) throws  Exception {
        File settings = new File(root, "settings.json");
        FileUtils.writeStringToFile(settings, obj.toString());

    }

    public void checkAndDownloadCampaign() {
        try {
            File data = new File(root, "data.json");

            if (data.exists()) {
                campaign = new Campaign(new JSONObject(FileUtils.readFileToString(data)).getJSONObject("campaign"));
            }
        } catch (Exception ex) {
            Log.e("MainService", ex.getMessage(), ex);
        }

        if (dTask.getStatus() != AsyncTask.Status.RUNNING && getUuid() != null) {
            dTask = new DownloadFilesTask();
            dTask.execute(String.format(DEFAULT_SERVER_JSON, getUuid()));
        }

    }

    public boolean isActive(){
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        List< ActivityManager.RunningTaskInfo > runningTaskInfo = manager.getRunningTasks(1);

        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;

        if(componentInfo.getPackageName().startsWith("ee.promobox.promoboxandroid")) {
            return true;
        }

        return false;
    }

    public Campaign getCampaign() {
        return campaign;
    }


    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    public void setSettings(JSONObject settings) {
        this.settings = settings;
    }

    public JSONObject getSettings() {
        return settings;
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
        this.uuid = uuid;
    }

    public class MainServiceBinder extends Binder {
        MainService getService() {
            return MainService.this;
        }
    }

    private class DownloadFilesTask extends AsyncTask<String, Integer, File> {


        protected File doInBackground(String... urls) {

            try {
                Campaign oldCamp = campaign;

                JSONObject data = loadData(urls[0]);

                if (data!=null) {

                    Campaign newCamp = new Campaign(data.getJSONObject("campaign"));

                    setOrientation(data.optInt("orientation", MainActivity.ORIENTATION_LANDSCAPE));

                    settings.put("orientation", getOrientation());

                    saveSettings(settings);

                    if (oldCamp == null || oldCamp.getCampaignId() != newCamp.getCampaignId() || (newCamp.getUpdateDate() > oldCamp.getUpdateDate())) {
                        campaign = newCamp;

                        downloadFiles();

                        Intent finish = new Intent(MainActivity.ACTIVITY_FINISH);

                        LocalBroadcastManager.getInstance(MainService.this).sendBroadcast(finish);

                        Intent update = new Intent(MainActivity.CAMPAIGN_UPDATE);

                        LocalBroadcastManager.getInstance(MainService.this).sendBroadcast(update);

                    } else {
                        downloadFiles();
                    }
                }

            } catch (Exception ex) {
                Log.e("MainService", ex.getMessage(), ex);
            }


            return null;
        }

        public void downloadFiles() {
            Log.i("MainService", "Download files");

            for (CampaignFile f : campaign.getFiles()) {
                File dir = new File(root.getAbsolutePath() + String.format("/%s/", campaign.getCampaignId()));
                dir.mkdirs();

                File file = new File(dir, f.getId() + "");

                if (!file.exists() || file.length() != f.getSize()) {

                    downloadFile(String.format(DEFAULT_SERVER + "/service/files/%s",  f.getId()), f.getId() + "");

                }
            }
        }

        private File downloadFile(String fileURL, String fileName) {
            try {

                Log.i("MainService", fileURL);

                HttpClient httpclient = new DefaultHttpClient();

                HttpGet httpget = new HttpGet(fileURL);

                HttpResponse response = httpclient.execute(httpget);

                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    File dir = new File(root.getAbsolutePath() + String.format("/%s/", campaign.getCampaignId()));

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
            long bytesAvailable = (long)stat.getBlockSize() *(long)stat.getBlockCount();

            json.put("freeSpace", bytesAvailable);
            json.put("force" , 1);


            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("json", json.toString()));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            Log.i("MainService", httppost.getRequestLine().toString());

            HttpResponse response = httpclient.execute(httppost);

            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    root.mkdirs();

                    File file = new File(root, "data.json");

                    FileOutputStream f = new FileOutputStream(file);

                    InputStream in = entity.getContent();

                    IOUtils.copy(in, f);

                    IOUtils.closeQuietly(f);
                    IOUtils.closeQuietly(in);

                    return new JSONObject(FileUtils.readFileToString(file));
                }

            }

            return null;
        }


        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(File result) {
        }
    }


}
