package ee.promobox.promoboxandroid.util;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.NetworkOnMainThreadException;
import android.util.Log;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Created by ilja on 16.02.2015.
 */
public class InternetConnectionUtil {


    private static final String TAG = "InternetConnectionUtil";

    /**
     * ConnectivityManager can be found in service.getSystemService(Context.CONNECTIVITY_SERVICE);
     */
    public static boolean isNetworkConnected(ConnectivityManager connectivityManager) {
        NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
        boolean result = true;
        if (ni == null) {
            // There are no active networks.
            result = false;
        }
        try {
            InetAddress ipAddress = InetAddress.getByName("www.google.com");

            if (ipAddress == null || ipAddress.toString().equals("")) {
                result = false;
            }

        } catch (UnknownHostException e) {
            result = false;
        } catch (NetworkOnMainThreadException e) {
            Log.w(TAG,  "NetworkOnMainThreadException");
        }
        return result;
    }

    public static boolean connectWifi(WifiData wifiData,  String networkPass, WifiManager wifiManager ){

        boolean wasConfigured = true;
        boolean returnValue;

        String networkSSID = wifiData.getNetworkSSID();
        Log.d(TAG, "connecting WIFI with SSID " + networkSSID);

        WifiConfiguration conf = getConfiguration(wifiData.getNetworkBSSID() , wifiManager);
        if (conf == null) {
            Log.d(TAG, "was NOT configured " + networkSSID);
            wasConfigured = false;
            conf = new WifiConfiguration();
            conf.SSID = "\"" + networkSSID + "\"";   // Please note the quotes. String should contain ssid in quotes
        } else {
            Log.d(TAG, "WAS configured " + networkSSID);
        }

        switch (wifiData.getSecurityMode()) {
            case WifiData.SECURITY_MODE_WPA:
//          For WPA network you need to add passphrase like this:
                conf.preSharedKey = "\"" + networkPass + "\"";

                break;
            case WifiData.SECURITY_MODE_WEP:
//          Then, for WEP network you need to do this:
                conf.wepKeys[0] = "\"" + networkPass + "\"";
                conf.wepTxKeyIndex = 0;
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);

                break;
            case WifiData.SECURITY_MODE_NONE:
//          For Open network you need to do this:
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                break;
        }

//        Then, you need to add it to Android wifi manager settings:
        int res = wasConfigured ? conf.networkId : wifiManager.addNetwork(conf);

//        And finally, you might need to enable it, so Android conntects to it:
        Log.d(TAG, "### 2 ### add Network returned " + res);

        wifiManager.enableNetwork(res, true);

        boolean changeHappen = wifiManager.saveConfiguration();

        if(res != -1 && changeHappen){
            Log.d(TAG, "### Change happen");
            returnValue = true;

        }else{
            Log.d(TAG, "*** Change NOT happen");
            wifiManager.removeNetwork(res);
            returnValue = false;
        }

        wifiManager.setWifiEnabled(true);

        return  returnValue;
    }

    private static WifiConfiguration getConfiguration(String networkSSID, WifiManager wifiManager){
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        if (list != null) {
            for( WifiConfiguration i : list ) {
                if(i.BSSID != null && i.BSSID.equals("\"" + networkSSID + "\"")) {
                    return  i;
                }
            }
        }
        return null;
    }


    public static class WifiData implements Serializable{
        private final String[] securityModes = { "WEP", "PSK", "EAP", "WPA" };
        public static final String SECURITY_MODE_NONE = "NONE";
        private static final String SECURITY_MODE_WEP = "WEP";
        private static final String SECURITY_MODE_PSK = "PSK";
        private static final String SECURITY_MODE_EAP = "EAP";
        private static final String SECURITY_MODE_WPA = "WPA";
        private boolean locked;
        private int signalStrength;
        private String name;
        private String securityMode;
        private String networkSSID;
        private String networkBSSID;

        public WifiData(ScanResult mScanResult) {
            name = mScanResult.SSID;
            signalStrength = WifiManager.calculateSignalLevel(mScanResult.level, 5);
            securityMode = checkLock(mScanResult.capabilities);
            locked = !securityMode.equals(SECURITY_MODE_NONE);
            this.networkSSID = mScanResult.SSID;
            this.networkBSSID = mScanResult.BSSID;
        }

        public boolean isLocked() {
            return locked;
        }

        public String getName() {
            return name;
        }

        public int getSignalStrength() {
            return signalStrength;
        }

        private String checkLock ( String capabilities){
            for (int i = securityModes.length - 1; i >= 0; i--) {
                if (capabilities.contains(securityModes[i])) {
                    return securityModes[i];
                }
            }
            return SECURITY_MODE_NONE;
        }

        public String getNetworkSSID() {
            return networkSSID;
        }

        public String getSecurityMode() {
            return securityMode;
        }

        public String getNetworkBSSID() {
            return networkBSSID;
        }
    }
}
