package ee.promobox.promoboxandroid;

import android.app.Service;
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
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


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

                URL u = new URL(fileURL);

                Log.i("Downloader", u.toString());

                HttpURLConnection c = (HttpURLConnection) u.openConnection();

                if (c.getResponseCode() != 200)
                    throw new Exception("Failed to connect");

                File dir = new File(root.getAbsolutePath() + String.format("/%s/", campaign.getCampaignId()));

                File file = new File(dir, fileName);

                FileOutputStream f = new FileOutputStream(file);

                InputStream in = c.getInputStream();

                IOUtils.copy(in, f);

                Log.i("test", "Size " + file.getAbsolutePath() + " = " + file.length());

                return file;
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
