package ee.promobox.promoboxandroid;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


public class MainService extends Service {

    private final static int TEST_CLIENT_ID = 1;
    private final String DEFAULT_SERVER = "http://46.182.31.94/%s/data.json";

    File root = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/promobox/");

    private final IBinder mBinder = new MyBinder();

    private Campaign campaign;

    private DownloadFilesTask dTask = new DownloadFilesTask();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        checkAndDownloadCampaign();

        return Service.START_NOT_STICKY;
    }

    public void checkAndDownloadCampaign() {
        try {
            File data = new File(root, "data.json");

            if (data.exists()) {
                campaign = new Campaign(new JSONObject(FileUtils.readFileToString(data)));
            }
        } catch (Exception ex) {
            Log.e("MainService", ex.getMessage(), ex);
        }

        if (dTask.getStatus() != AsyncTask.Status.RUNNING) {
            dTask = new DownloadFilesTask();
            dTask.execute(String.format(DEFAULT_SERVER, TEST_CLIENT_ID));
        }

    }

    public boolean isForeground(String myPackage){
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        List< ActivityManager.RunningTaskInfo > runningTaskInfo = manager.getRunningTasks(1);

        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;

        if(componentInfo.getPackageName().startsWith(myPackage)) {
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

    public class MyBinder extends Binder {
        MainService getService() {
            return MainService.this;
        }
    }

    private class DownloadFilesTask extends AsyncTask<String, Integer, File> {


        protected File doInBackground(String... urls) {

            try {
                Campaign oldCamp = campaign;
                Campaign newCamp = loadCampaign(urls[0]);

                if (oldCamp == null || (newCamp.getUpdateId() > oldCamp.getUpdateId())) {
                    campaign = newCamp;

                    downloadFiles();

                    Intent finish = new Intent(MainActivity.ACTIVITY_FINISH);

                    LocalBroadcastManager.getInstance(MainService.this).sendBroadcast(finish);

                    Intent update = new Intent(MainActivity.CAMPAIGN_UPDATE);

                    LocalBroadcastManager.getInstance(MainService.this).sendBroadcast(update);

                } else {
                    downloadFiles();
                }

            } catch (Exception ex) {
                Log.e("MainService", ex.getMessage(), ex);
            }

            return null;
        }

        public void downloadFiles() {
            for (CampaignFile f : campaign.getFiles()) {
                File dir = new File(root.getAbsolutePath() + String.format("/%s/", campaign.getCampaignId()));
                dir.mkdirs();

                File file = new File(dir, f.getName());

                if (!file.exists() || file.length() != f.getSize()) {

                    downloadFile(String.format("http://46.182.31.94/%s/%s/%s", campaign.getClientId(), campaign.getCampaignId(),f.getName()), f.getName());

                }
            }
        }

        private File downloadFile(String fileURL, String fileName) {
            try {

                Log.i("Downloader", fileURL);

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

                    Log.i("test", "Size " + file.getAbsolutePath() + " = " + file.length());

                    return file;
                }

            } catch (Exception e) {
                Log.d("Downloader", e.getMessage(), e);
            }

            return null;

        }

        private Campaign loadCampaign(String url) throws Exception {
            URL u = new URL(url);

            HttpURLConnection c = (HttpURLConnection) u.openConnection();

            if (c.getResponseCode() != 200)
                throw new Exception("Failed to connect");

            root.mkdirs();

            File file = new File(root, FilenameUtils.getName(u.getFile()));

            FileOutputStream f = new FileOutputStream(file);

            InputStream in = c.getInputStream();

            IOUtils.copy(in, f);

            f.close();
            in.close();

            return new Campaign(new JSONObject(FileUtils.readFileToString(file)));
        }


        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(File result) {
        }
    }


}
