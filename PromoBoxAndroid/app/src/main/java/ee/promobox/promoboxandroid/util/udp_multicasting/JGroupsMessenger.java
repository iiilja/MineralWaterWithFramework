package ee.promobox.promoboxandroid.util.udp_multicasting;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.FileHandler;

import ee.promobox.promoboxandroid.util.udp_multicasting.messages.MultiCastMessage;

/**
 * Created by ilja on 3.03.2015.
 */
public class JGroupsMessenger extends ReceiverAdapter{

    private final String TAG = "JGroupsMessenger";
    private final String CLUSTER_NAME = "PromoboxCluster";
    private JChannel channel;
    private final Handler incomingMessageHandler;

    public JGroupsMessenger(Handler messageReceivedHandler){
        incomingMessageHandler = messageReceivedHandler;
    }

    public void start(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if(wifiManager != null) {
            WifiManager.MulticastLock mcLock = wifiManager.createMulticastLock(CLUSTER_NAME);
            mcLock.acquire();
        }
        try {
            channel=new JChannel(context.getAssets().open("jGroupConfig.xml"));
            channel.setReceiver(this);
            channel.connect(CLUSTER_NAME);
        } catch (Exception e){
            e.printStackTrace();
        }


    }

    private void stop(){
        channel.close();
    }

    public void sendMessage(MultiCastMessage message){
        try {
            Message msg=new Message(null,message.getBytes());
            channel.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void viewAccepted(View view) {
        Log.d(TAG, "** view: " + view);
    }



    @Override
    public void receive(Message msg) {
        MultiCastMessage multiCastMessage = MultiCastMessage.newInstance(msg.getBuffer());
        Log.d(TAG, multiCastMessage.toString());
        android.os.Message message = new android.os.Message();
        message.obj = multiCastMessage;
        incomingMessageHandler.sendMessage(message);
    }

//    private class MyJChannel extends JChannel{
//
//        public MyJChannel() throws Exception {
//            super();
//            Object loopback1 = getProtocolStack().getUpProtocol().getValue("loopback");
//            Object loopback2 = getProtocolStack().getUpProtocol().getValue("Loopbaack");
//            String loopbackString1 = loopback1 != null ? loopback1.toString(): "null";
//            String loopbackString2 = loopback2 != null ? loopback2.toString(): "null";
//            Log.d(TAG, " lopback1 =  " + loopbackString1 + " loopback2"  + loopbackString2);
////            if (config.containsKey("loopback") || config.containsKey("Loopback")){
////                Log.d(TAG, " contains " + config.get("loopback").toString());
////
////            } else {
////                Log.d(TAG, "NOT contains");
////                config.put("loopback",true);
////                config.put("Loopback",true);
////            }
//        }
//    }
}
