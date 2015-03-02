package ee.promobox.promoboxandroid.util.udp_multicasting;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import ee.promobox.promoboxandroid.util.udp_multicasting.messages.*;

/**
 * Created by ilja on 25.02.2015.
 */
public class UDPMessenger{

    protected static String DEBUG_TAG = "UDPMessenger"; // to log out things
    protected static final Integer BUFFER_SIZE = 21; // size of the reading buffer

    protected String TAG; // chat TAG
    protected int MULTI_CAST_PORT; // chat port

    private boolean receiveMessages = false; // variable to know if we have to listen for incoming packets

    protected Context context; // the application's context, used to get network state info etc.
    private ConnectivityManager connManager;
    private WifiManager wifiManager;
    private DatagramSocket socket; // the socket used to send the messages

//    protected abstract Runnable getIncomingMessageAnalyseRunnable(); // the abstract runnable which will analyse the incoming packets
    private final Handler incomingMessageHandler; // the handler which will start the previous one
    protected MultiCastMessage incomingMessage; // the n-th incoming message to be analysed - Message is a custom class
    private Thread receiverThread; // the thread used to receive the multicast
//    It may appear confusing, but it will soon be clearer when you'll see what each variable will be used for :)


    /**
     * Class constructor
     * @param context the application's context
     * @param tag a valid string, used to filter the UDP broadcast messages (in and out). It can't be null or 0-characters long.
     * @param multiCastPort the port to multicast to. Must be between 1025 and 49151 (inclusive)
     *  connectionPort the port to get the connection back. Must be between 1025 and 49151
     */
    public UDPMessenger(Context context, Handler messageReceivedHandler, String tag, int multiCastPort) throws IllegalArgumentException {
        if(context == null || tag == null || tag.length() == 0 ||
                multiCastPort <= 1024 || multiCastPort > 49151)
            throw new IllegalArgumentException();
        this.context = context.getApplicationContext();
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        TAG = tag;
        MULTI_CAST_PORT = multiCastPort;

        incomingMessageHandler = messageReceivedHandler;
    }

    /**
     * Sends a broadcast message (TAG EPOCH_TIME message). Opens a new socket in case it's closed.
     * @param message the message to send (multicast). It can't be null or 0-characters long.
     * @return
     * @throws IllegalArgumentException
     */
    public boolean sendMessage(MultiCastMessage message) throws IllegalArgumentException {
        if(message == null || message.getBytes().length == 0)
            throw new IllegalArgumentException();

        // Check for WiFi connectivity
//        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//        NetworkInfo mLan = connManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);



//        if(mWifi == null || !mWifi.isConnected())
//        {
//            Log.w(DEBUG_TAG, "Sorry! You need to be in a WiFi network in order to send UDP multicast packets. Aborting.");
//            return false;
//        }

        // Check for IP address
        String ipAddress = getIpAddress();
        Log.d(TAG,ipAddress);
        int last = ipAddress.lastIndexOf(".");
        ipAddress = ipAddress.substring(0,last+1) + "255";
        Log.d(TAG, ipAddress);

//        int ip = wifiManager.getConnectionInfo().getIpAddress();

        // Create the send socket
        try {
            if (socket == null) {
                socket = new DatagramSocket();
            }
            // Build the packet
            DatagramPacket packet;
            byte data[] = message.getBytes();
            Log.d(DEBUG_TAG, "SENT  " + MultiCastMessage.newInstance(data));

            try {
                packet = new DatagramPacket(data, data.length, InetAddress.getByName(ipAddress), MULTI_CAST_PORT);
            } catch (UnknownHostException e) {
                Log.d(DEBUG_TAG, "It seems that " + ipAddress + " is not a valid ip! Aborting.");
                e.printStackTrace();
                return false;
            }

            SendMessageAsyncTask sendMessageAsyncTask = new SendMessageAsyncTask();
            sendMessageAsyncTask.execute(packet);

            return true;
        } catch (SocketException e) {
            Log.d(DEBUG_TAG, "There was a problem creating the sending socket. Aborting.");
            e.printStackTrace();
            return false;
        }
    }

    public static String ipToString(int ip, boolean broadcast) {
//        return "192.168.43.255";
        String result = new String();

        Integer[] address = new Integer[4];
        for(int i = 0; i < 4; i++)
            address[i] = (ip >> 8*i) & 0xFF;
        for(int i = 0; i < 4; i++) {
            if(i != 3)
                result = result.concat(address[i]+".");
            else result = result.concat("255");
        }
        return result.substring(0, result.length()-1);
    }

    public static String getIpAddress() {
        try {
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()&&inetAddress instanceof Inet4Address) {
                        String ipAddress=inetAddress.getHostAddress();
                        Log.e("IP address",""+ipAddress);
                        return ipAddress;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("Socket exception in GetIP Address of Utilities", ex.toString());
        }
        return null;
    }

    public void startMessageReceiver() {
        Runnable receiver = new Runnable() {

            @Override
            public void run() {
                if(wifiManager != null) {
                    WifiManager.MulticastLock mcLock = wifiManager.createMulticastLock(TAG);
                    mcLock.acquire();
                }

                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket rPacket = new DatagramPacket(buffer, buffer.length);
                MulticastSocket rSocket;

                try {
                    rSocket = new MulticastSocket(MULTI_CAST_PORT);
                } catch (IOException e) {
                    Log.d(DEBUG_TAG, "Impossible to create a new MulticastSocket on port " + MULTI_CAST_PORT);
                    e.printStackTrace();
                    return;
                }

                while(receiveMessages) {
                    try {
                        rSocket.receive(rPacket);
                    } catch (IOException e1) {
                        Log.d(DEBUG_TAG, "There was a problem receiving the incoming message.");
                        e1.printStackTrace();
                        continue;
                    }

                    if(!receiveMessages)
                        break;

                    byte data[] = rPacket.getData();

                    incomingMessage = MultiCastMessage.newInstance(data);

                    Log.d(DEBUG_TAG, "RECEIVED MESSAGE " + incomingMessage.toString());
                    Message message = new Message();
                    message.obj = incomingMessage;
                    incomingMessageHandler.sendMessage(message);
                }
            }

        };

        receiveMessages = true;
        if(receiverThread == null)
            receiverThread = new Thread(receiver);

        if(!receiverThread.isAlive())
            receiverThread.start();
    }

    public void stopMessageReceiver() {
        receiveMessages = false;
    }

    public boolean isReceiveMessages() {
        return receiveMessages;
    }


    private class SendMessageAsyncTask extends AsyncTask<DatagramPacket,Void,Void>{


        @Override
        protected Void doInBackground(DatagramPacket... params) {
            try {
                Log.d(DEBUG_TAG, "Sending to socket");
                socket.send(params[0]);
            } catch (IOException e) {
                Log.d(DEBUG_TAG, "There was an error sending the UDP packet. Aborted.");
                e.printStackTrace();
                return null;
            }
            return null;
        }
    }
}
