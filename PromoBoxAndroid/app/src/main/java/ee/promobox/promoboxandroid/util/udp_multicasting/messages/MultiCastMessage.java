package ee.promobox.promoboxandroid.util.udp_multicasting.messages;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Created by ilja on 25.02.2015.
 */
public abstract class MultiCastMessage {
    public static final byte MASTER = 1;
    public static final byte PREPARE = 2;
    public static final byte PLAY = 3;

    protected static final String MASTER_STRING = "MASTER";
    protected static final String PREPARE_STRING = "PREPARE";
    protected static final String PLAY_STRING = "PLAY";

    protected static final int MASTER_LENGTH = 5;
    protected static final int PREPARE_LENGTH = 13;
    protected static final int PLAY_LENGTH = 21;

    private byte type;
    private String deviceId;

    public static MultiCastMessage newInstance(byte[] bytes){
        if (bytes.length != MASTER_LENGTH && bytes.length != PREPARE_LENGTH && bytes.length != PLAY_LENGTH) {
            throw new IllegalArgumentException("Bytes length is " + bytes.length + " " + bytes.toString());
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int type = buffer.get();
        byte[] deviceIdChars = new byte[4];
        for (int i = 0; i < 4; i++) {
            deviceIdChars[i] = buffer.get();
        }
        String deviceId = new String(deviceIdChars,Charset.forName("UTF-8"));
        int campaignId;
        int fileId;
        long frameNr;
        switch (type){
            case MASTER:
                return new MasterMessage(deviceId);
            case PREPARE:
                campaignId = buffer.getInt();
                fileId = buffer.getInt();
                return new PrepareMessage(deviceId, campaignId, fileId);
            case PLAY:
                campaignId = buffer.getInt();
                fileId = buffer.getInt();
                frameNr = buffer.getLong();
                return new PlayMessage(deviceId,campaignId,fileId,frameNr);
        }
        return null;
    }

    public byte getType() {
        return type;
    }

    protected void setType(byte type) {
        this.type = type;
    }

    public String getDeviceId() {
        return deviceId;
    }

    protected void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public abstract byte[] getBytes();
}
