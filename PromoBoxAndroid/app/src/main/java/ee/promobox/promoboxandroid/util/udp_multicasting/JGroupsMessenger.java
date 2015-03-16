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
import org.jgroups.blocks.locking.LockNotification;
import org.jgroups.blocks.locking.LockService;
import org.jgroups.util.Owner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.logging.FileHandler;

import ee.promobox.promoboxandroid.util.udp_multicasting.messages.MultiCastMessage;

/**
 * Created by ilja on 3.03.2015.
 */
public class JGroupsMessenger extends ReceiverAdapter implements LockNotification{

    private final String TAG = "JGroupsMessenger";
    private final String CLUSTER_NAME = "PromoboxCluster";

    private Thread statusPrinter;
    private LockService lockService;
    private Lock masterLock;
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
            lockService = new LockService(channel);
            lockService.addLockListener( this );
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

    @Override
    public void viewAccepted(View view) {
        Log.d(TAG, "** view: " + view);
        if (masterLock != null){
//            startAcquiringThread();
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

    private void getLock()
    {
        masterLock = lockService.getLock("master");

        isMaster.set(masterLock.tryLock());
        if (isMaster.get()){
            Log.d(TAG,"I HAVE become the master!");
        } else {
            Log.d(TAG,"I could NOT become the master!");
        }
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
                        Log.d(TAG,"cluster view [" + channel.getViewAsString() + "]");
                        sleep(2000L);
                        if (!isMaster.get()){
                            getLock();
                        }
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

    @Override
    public void lockCreated(String s) {

    }

    @Override
    public void lockDeleted(String s) {

    }

    @Override
    public void locked(String s, Owner owner) {

    }

    @Override
    public void unlocked(String s, Owner owner) {

    }

    @Override
    public void awaiting(String s, Owner owner) {

    }

    @Override
    public void awaited(String s, Owner owner) {

    }
}
