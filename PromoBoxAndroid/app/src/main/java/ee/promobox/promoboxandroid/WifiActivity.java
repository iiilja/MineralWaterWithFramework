package ee.promobox.promoboxandroid;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ee.promobox.promoboxandroid.util.InternetConnectionUtil;
import ee.promobox.promoboxandroid.util.InternetConnectionUtil.WifiData;
import ee.promobox.promoboxandroid.widgets.WifiPasswordDialog;


public class WifiActivity extends ActionBarActivity implements View.OnClickListener, AdapterView.OnItemClickListener{

    private static final String TAG = "WifiActivity";

    private ListView wifiListView;
    private RadioButton wifiRadioButton;

    private List<WifiData> wifiDataList;
    private WifiManager wifiManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiListView = (ListView) findViewById(R.id.network_wifi_list);
        wifiRadioButton = (RadioButton) findViewById(R.id.network_radio_button_wifi);

        Button skipButton = (Button) findViewById(R.id.network_skip_btn);
        skipButton.setOnClickListener(this);

        updateWifiList(wifiManager);
        wifiListView.setAdapter(new WifiListAdapter(getBaseContext(), wifiDataList));
        wifiListView.setOnItemClickListener(this);
    }

    private void updateWifiList(WifiManager wifiManager){
        wifiDataList = new ArrayList<>();
        List<ScanResult> mScanResults = wifiManager.getScanResults();
        for (ScanResult mScanResult : mScanResults) {
            WifiData wifiData = new WifiData(mScanResult);
            wifiDataList.add(wifiData);
        }
    }

    private Drawable getNetworkWifiLockDrawable(int signalStrength , boolean locked) {
        try {
            String identifierName  = String.format("drawable/wifi%d%s", signalStrength, (locked ? "_lock" : ""));

            int id = getResources().getIdentifier( identifierName , null, getPackageName());
            return getResources().getDrawable(id);
        } catch (Resources.NotFoundException e) {

            Log.e(TAG, getResources().getResourceName(R.drawable.wifi0_lock));
            Log.e(TAG, e.getMessage());
            return getResources().getDrawable(R.drawable.wifi0_lock);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.network_skip_btn:
                WifiActivity.this.finish();
                return;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

        WifiData wifiData = wifiDataList.get(position);

        TextView wifiStatus = (TextView) view.findViewById(R.id.network_wifi_status);
        TextView wifiName = (TextView) view.findViewById(R.id.network_wifi_name);
        Log.w(TAG, "Setting Connecting... to " + wifiName.getText());
        wifiStatus.setText(getString(R.string.network_connecting));

        if (wifiData.getSecurityMode().equals(WifiData.SECURITY_MODE_NONE)) {
            connectToWifi(wifiData, "", position);
        } else {
            showDialog(wifiData, position);
        }
    }

    public void connectToWifi(WifiData wifiData, String password, int listViewElementPosition) {
        Log.d(TAG, "Connecting to wifi with password :" + password + " to element " + listViewElementPosition + " " + wifiData.getName());
        View view = getViewByPosition(listViewElementPosition,wifiListView);
        final TextView wifiStatus = (TextView) view.findViewById(R.id.network_wifi_status);

        if (InternetConnectionUtil.connectWifi(wifiData,password, wifiManager)) {

            final TextView wifiName = (TextView) view.findViewById(R.id.network_wifi_name);

            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (cm.getActiveNetworkInfo() != null ) {
                        wifiStatus.setText(getString(R.string.network_connected));
                        wifiName.setTextColor(getResources().getColor(R.color.myYellowButtonColor));
                        wifiRadioButton.setChecked(true);
//                        Intent returnIntent = new Intent();
//                        returnIntent.putExtra("result", MainActivity.RESULT_FINISH_FIRST_START);
//                        setResult(RESULT_OK, returnIntent);
                        WifiActivity.this.finish();
                    } else {
                        wifiStatus.setText(getString(R.string.network_disconnected));
                    }
                }
            },5000);
        } else {
            wifiStatus.setText(getString(R.string.network_disconnected));
            Toast.makeText(getBaseContext(), "failed connecting", Toast.LENGTH_LONG).show();
        }
    }

    void showDialog(WifiData wifiData, int listViewElementPosition) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        WifiPasswordDialog newFragment = WifiPasswordDialog.newInstance(wifiData, listViewElementPosition);
        newFragment.show(ft, "dialog");
    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }


    private class WifiListAdapter extends BaseAdapter {

        Context context;
        private List<WifiData> wifiDataList;
        private LayoutInflater inflater = null;

        public WifiListAdapter(Context context, List<WifiData> wifiDataList) {
            // TODO Auto-generated constructor stub
            this.context = context;
            this.wifiDataList = wifiDataList;
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return wifiDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return wifiDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            View vi = convertView;
            if (vi == null) {
                vi = inflater.inflate(R.layout.wifi_row_layout, null);
            }
            TextView text = (TextView) vi.findViewById(R.id.network_wifi_name);
            ImageView imageView = (ImageView) vi.findViewById(R.id.network_wifi_lock_image);

            WifiData wifiData = wifiDataList.get(position);

            text.setText(wifiData.getName());
            imageView.setImageDrawable(getNetworkWifiLockDrawable(wifiData.getSignalStrength(),wifiData.isLocked()));

            return vi;
        }
    }



}
