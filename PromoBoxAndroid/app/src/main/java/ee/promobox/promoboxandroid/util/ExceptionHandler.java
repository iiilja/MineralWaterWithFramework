package ee.promobox.promoboxandroid.util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import ee.promobox.promoboxandroid.MainActivity;
import ee.promobox.promoboxandroid.MainService;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final String TAG = "ExceptionHandler";
    private final Context myContext;
    private final boolean startActivity;
    private final String URL;
    private final String LINE_SEPARATOR = "\n";

    public ExceptionHandler(Context context, boolean startActivity, String UUID) {
        myContext = context;
        this.startActivity = startActivity;
        URL = MainService.DEFAULT_SERVER + String.format("/service/device/%s/saveError",UUID);
    }

    public void uncaughtException(Thread thread, final Throwable exception) {
        StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));
        final StringBuilder errorReport = new StringBuilder();
        errorReport.append("CAUSE OF ERROR ");
        errorReport.append(stackTrace.toString());

//        errorReport.append("\n************ DEVICE INFORMATION ***********\n");
//        errorReport.append("Brand: ");
//        errorReport.append(Build.BRAND);
//        errorReport.append(LINE_SEPARATOR);
//        errorReport.append("Device: ");
//        errorReport.append(Build.DEVICE);
//        errorReport.append(LINE_SEPARATOR);
//        errorReport.append("Model: ");
//        errorReport.append(Build.MODEL);
//        errorReport.append(LINE_SEPARATOR);
//        errorReport.append("Id: ");
//        errorReport.append(Build.ID);
//        errorReport.append(LINE_SEPARATOR);
//        errorReport.append("Product: ");
//        errorReport.append(Build.PRODUCT);
//        errorReport.append(LINE_SEPARATOR);
//        errorReport.append("\n************ FIRMWARE ************\n");
//        errorReport.append("SDK: ");
//        errorReport.append(Build.VERSION.SDK);
//        errorReport.append(LINE_SEPARATOR);
//        errorReport.append("Release: ");
//        errorReport.append(Build.VERSION.RELEASE);
//        errorReport.append(LINE_SEPARATOR);
//        errorReport.append("Incremental: ");
//        errorReport.append(Build.VERSION.INCREMENTAL);
//        errorReport.append(LINE_SEPARATOR);

        Log.e(TAG, errorReport.toString());


        if (!startActivity){
            sendError(exception, errorReport);
        }


        if (startActivity) {
            Log.d(TAG,"Sending intent to start MainActivity");
            Intent intent = new Intent(myContext, MainActivity.class);
            intent.putExtra("error", errorReport.toString());
            myContext.startActivity(intent);
        } else {
            Log.d(TAG,"Sending intent to start MainService");
            Intent intent = new Intent(myContext, MainService.class);
            myContext.startService(intent);
        }

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }

    private void sendError(Throwable exception, StringBuilder errorReport){
        try {
            JSONObject json = new JSONObject();
            json.put("name", "UncaughtException");
            json.put("message", exception.getMessage());
            json.put("date", System.currentTimeMillis());
            json.put("stackTrace", errorReport.toString().replace("at java.util.",""));


            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(URL);
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("error", json.toString()));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            Log.d(TAG, "URL = " + URL);
            Log.d(TAG, "Request = " + IOUtils.toString(httpPost.getEntity().getContent()));
            HttpResponse response = httpclient.execute(httpPost);
            Log.d(TAG, "Response = " + IOUtils.toString(response.getEntity().getContent()));

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
