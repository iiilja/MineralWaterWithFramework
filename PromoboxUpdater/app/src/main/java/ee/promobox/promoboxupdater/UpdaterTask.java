package ee.promobox.promoboxupdater;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ilja on 12.03.2015.
 */
public class UpdaterTask extends AsyncTask<Void,Void,Boolean> {
    private static final String TAG = "UpdaterTask";
    private String fileUrl = "http://www.tud.ttu.ee/web/Ilja.Denissov/promobox/promobox_%s.apk";
    public static final String APK_FILE_NAME = "promobox.apk";
    private MainService service;

    public UpdaterTask(MainService service){
        this.service = service;
    }

    private String getActualVersion(){
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(MainService.DEFAULT_SERVER_VERSION);
        HttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (response.getStatusLine().getStatusCode() == 200 && entity != null) {
                String jsonString = IOUtils.toString(response.getEntity().getContent());
                JSONObject json = new JSONObject(jsonString);
                if (json.has(MainService.VERSION)){
                    return json.getString(MainService.VERSION);
                } else {
                    return MainService.VERSION_0;
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return MainService.VERSION_0;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        String installedVersion = service.getInstalledAppVersion();
        String actualVersion = getActualVersion();
        if (actualVersion.equals(installedVersion)){
            Log.d(TAG, "Versions are equal - " + actualVersion);
        } else {
            Log.d(TAG, "Versions NOT equal actual = " + actualVersion + " installed = " + installedVersion);
            boolean downloaded = downloadFile(String.format(fileUrl,actualVersion), APK_FILE_NAME);
            return true;
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (aBoolean){
            Intent mainActivity = new Intent(service.getBaseContext(), MainActivity.class);

            mainActivity.setAction(Intent.ACTION_MAIN);
            mainActivity.addCategory(Intent.CATEGORY_LAUNCHER);
            mainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
            mainActivity.putExtra("install",true);

            service.startActivity(mainActivity);


//            service.setCurrentVersion(service.getCurrentVersion());
//            Intent promptInstall = new Intent(Intent.ACTION_VIEW);
//            Uri uri = Uri.fromFile(new File(service.getROOT().getAbsolutePath(), APK_FILE_NAME));
//            promptInstall.setDataAndType(uri,
//                            "application/vnd.android.package-archive");
//            promptInstall.setFlags(Intent.FLAG_FROM_BACKGROUND);
//            service.startActivity(promptInstall);
        }
    }

    private boolean downloadFile(String fileURL, String fileName) {
        try {
            Log.i(TAG, fileURL);

            HttpClient httpclient = new DefaultHttpClient();

            HttpGet httpget = new HttpGet(fileURL);

            HttpResponse response = httpclient.execute(httpget);

            HttpEntity entity = response.getEntity();

            if (entity != null) {
                File dir = new File(service.getROOT().getAbsolutePath());

                File file = new File(dir, fileName);

                FileOutputStream f = new FileOutputStream(file);

                InputStream in = entity.getContent();

                IOUtils.copy(in, f);

                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(f);

                Log.i(TAG, "Size " + file.getAbsolutePath() + " = " + file.length());

                return true;
            }


        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);
        }
        return false;
    }
}
