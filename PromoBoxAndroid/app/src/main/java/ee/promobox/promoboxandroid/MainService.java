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
import java.util.Enumeration;
import java.util.List;


public class MainService extends Service {


    private final String DEFAULT_SERVER = "http://api.promobox.ee/";
    private final String DEFAULT_SERVER_JSON = DEFAULT_SERVER + "/service/device/%s/pull";

    private SharedPreferences sharedPref;

    private String uuid;
    private int orientation;
    private boolean alwaysOnTop = false;

    File root = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/promobox/");

    private final IBinder mBinder = new MainServiceBinder();

    private Campaign campaign;

    private DownloadFilesTask dTask = new DownloadFilesTask();

    @Override
    public void onCreate() {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i("MainService","Start command");

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

                    if (data.has("campaign")) {

                        Campaign newCamp = new Campaign(data.getJSONObject("campaign"));

                        setOrientation(data.optInt("orientation", MainActivity.ORIENTATION_LANDSCAPE));

                        sharedPref.edit().putInt("orientation", orientation).commit();

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
                    if (orientation!=MainActivity.ORIENTATION_PORTRAIT_EMULATION) {
                        downloadFile(String.format(DEFAULT_SERVER + "/service/files/%s", f.getId()), f.getId() + "");
                    } else {

                        File filePort = new File(dir, f.getId() + "_port");

                        if (!filePort.exists()) {
                            downloadFile(String.format(DEFAULT_SERVER + "/service/files/%s?orient=3", f.getId()), f.getId() + "_port");
                        }
                    }

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
            long bytesAvailable = (long)stat.getBlockSize() *(long)stat.getAvailableBlocks();


            final Enumeration<NetworkInterface> listNetworks =  NetworkInterface.getNetworkInterfaces();

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
            json.put("force" , 1);


            Log.i("MainService", "Pull info:" + json.toString());


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
