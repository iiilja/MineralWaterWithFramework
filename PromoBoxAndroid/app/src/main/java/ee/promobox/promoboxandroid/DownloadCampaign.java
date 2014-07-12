package ee.promobox.promoboxandroid;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class DownloadCampaign extends IntentService {

    private static final String ACTION_DOWNLOAD_CAMPAIGN = "ee.promobox.promoboxandroid.action.DOWNLOAD_CAMPAIGN";

    private static final String CLIENT_ID = "ee.promobox.promoboxandroid.extra.CLIENT_ID";
    private static final String UPDATE_ID = "ee.promobox.promoboxandroid.extra.UPDATE_ID";
    private static final String RECEIVER = "ee.promobox.promoboxandroid.extra.RECEIVER";



    public static void startAction(Context context, int clientId, int updateId ) {
        Intent intent = new Intent(context, DownloadCampaign.class);

        intent.setAction(ACTION_DOWNLOAD_CAMPAIGN);

        intent.putExtra(CLIENT_ID, clientId);
        intent.putExtra(UPDATE_ID, updateId);

        context.startService(intent);
    }


    public DownloadCampaign() {
        super("DownloadCampaign");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();

            if (ACTION_DOWNLOAD_CAMPAIGN.equals(action)) {

                final int clientId = intent.getIntExtra(CLIENT_ID, 0);
                final int updateId = intent.getIntExtra(UPDATE_ID, 0);

                handleActionFoo(clientId, updateId);

            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */

    private JSONObject getCampaignJson(int clientId) throws Exception {

        File jsonFile = downloadFile(String.format("http://46.182.31.94/%s/data.json", clientId), "data.json");

        InputStream is = new FileInputStream(jsonFile);

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        IOUtils.copy(is, os);

        os.flush();

        JSONObject campaignJson = new JSONObject(new String(os.toByteArray()));

        os.close();
        is.close();

        return campaignJson;

    }

    public File downloadFile(String fileURL, String fileName) {
        try {
            File root = Environment.getExternalStorageDirectory();
            URL u = new URL(fileURL);

            HttpURLConnection c = (HttpURLConnection) u.openConnection();

            if (c.getResponseCode() != 200)
                throw new Exception("Failed to connect");

            File file = new File(root, fileName);

            FileOutputStream f = new FileOutputStream(file);

            InputStream in = c.getInputStream();

            IOUtils.copy(in, f);

            return file;
        } catch (Exception e) {
            Log.d("Downloader", e.getMessage(), e);
        }

        return null;

    }

    private void handleActionFoo(int clientId, int updateId) {
        try {

            JSONObject campaignJson = getCampaignJson(clientId);

            if (updateId < campaignJson.getInt("updateId")) {

                Log.i("test", campaignJson.toString());

                int campaignId = campaignJson.getInt("campaignId");

                JSONArray files = campaignJson.getJSONArray("files");

                for (int i = 0; i < files.length(); i++) {
                    JSONObject file = files.getJSONObject(i);

                    File f = downloadFile(String.format("http://46.182.31.94/%s/%s/%s", clientId, campaignId, file.getString("path")), file.getString("path"));
                    Log.i("test", "Size: " + f.length());

                }
            }


        } catch (Exception ex) {
            Log.e("DownloadCampaign", ex.getMessage(), ex);
        }


    }



}
