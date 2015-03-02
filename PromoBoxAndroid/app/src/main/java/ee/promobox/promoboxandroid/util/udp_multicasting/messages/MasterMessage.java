package ee.promobox.promoboxandroid.util.udp_multicasting.messages;

import java.nio.ByteBuffer;

/**
 * Created by ilja on 25.02.2015.
 */
public class MasterMessage extends MultiCastMessage {

    public MasterMessage(String deviceId) {
        setType(MASTER);
        setDeviceId(deviceId);
    }

    @Override
    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(MASTER_LENGTH);
        buffer.put(getType());
        buffer.put(getDeviceId().getBytes());
        return buffer.array();
    }

    @Override
    public String toString() {
        return MASTER_STRING
                + " deviceId = " + getDeviceId();
    }
}
