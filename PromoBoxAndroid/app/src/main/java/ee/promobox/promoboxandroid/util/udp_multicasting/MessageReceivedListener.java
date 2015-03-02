package ee.promobox.promoboxandroid.util.udp_multicasting;

import ee.promobox.promoboxandroid.util.udp_multicasting.messages.MasterMessage;
import ee.promobox.promoboxandroid.util.udp_multicasting.messages.PlayMessage;
import ee.promobox.promoboxandroid.util.udp_multicasting.messages.PrepareMessage;

/**
 * Created by ilja on 26.02.2015.
 */
public interface MessageReceivedListener {
    public void onPlayMessageReceived(PlayMessage message);
    public void onPrepareMessageReceived(PrepareMessage message);
//    public void onMasterMessageReceived(MasterMessage message);
}
