package ee.promobox.promoboxandroid.util.udp_multicasting;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.blocks.locking.LockNotification;
import org.jgroups.blocks.locking.LockService;
import org.jgroups.util.Owner;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

import ee.promobox.promoboxandroid.util.udp_multicasting.messages.MultiCastMessage;

/**
 * Created by ilja on 3.03.2015.
 */
public class JGroupsMessenger extends ReceiverAdapter{

    private final String TAG = "JGroupsMessenger";
    private final String CLUSTER_NAME = "PromoboxCluster";

    private Thread statusPrinter;
    private AtomicBoolean isMaster = new AtomicBoolean(false);
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
            startStatusPrintingThread();
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

    public String getAddress4Char(){
        String address = channel.getAddressAsString();
        return address.substring(address.length() - 4);
    }

    @Override
    public void viewAccepted(View view) {
        boolean wasMaster = isMaster.get();
        Log.d(TAG, "** view: " + view);
        Log.d(TAG, "** My address: " + channel.getAddress());
        Log.d(TAG, "** First address: " + view.getMembers().get(0));
        if (channel.getAddress().equals(view.getMembers().get(0))){
            isMaster.set(true);
            Log.d(TAG,"I HAVE become the master!");
        } else {
            isMaster.set(false);
            Log.d(TAG,"I could NOT become the master!");
        }
        if (wasMaster != isMaster.get()){
            Log.d(TAG, "Sending master message : isMaster = " + isMaster.get());
            android.os.Message message = new android.os.Message();
            message.obj = isMaster.get();
            incomingMessageHandler.sendMessage(message);
        }
    }



    @Override
    public void receive(Message msg) {
        MultiCastMessage multiCastMessage = MultiCastMessage.newInstance(msg.getBuffer());
        Log.d(TAG, multiCastMessage.toString());
        android.os.Message message = new android.os.Message();
        message.obj = multiCastMessage;
        incomingMessageHandler.sendMessage(message);
    }


    private void startStatusPrintingThread()
    {
        statusPrinter = new Thread() {
            @Override
            public void run()
            {
                Thread.currentThread().setName("status-printer");

                //noinspection InfiniteLoopStatement
                while ( true ) {
                    try
                    {
                        Log.d(TAG, "is master [" + isMaster + "]");
                        Log.d(TAG, "cluster view [" + channel.getViewAsString() + "]");
                        Log.d(TAG, channel.getAddressAsString());

                        sleep(10000L);
                    }
                    catch ( InterruptedException e )
                    {
                        // If the sleep gets interrupted, that's fine.
                    }
                }
            }
        };

        statusPrinter.setDaemon(true);
        statusPrinter.start();
    }
}
