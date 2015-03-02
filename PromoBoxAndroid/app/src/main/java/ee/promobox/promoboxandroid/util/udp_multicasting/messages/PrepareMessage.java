package ee.promobox.promoboxandroid.util.udp_multicasting.messages;

import java.nio.ByteBuffer;

/**
 * Created by ilja on 25.02.2015.
 */
public class PrepareMessage extends MultiCastMessage {

    private int campaignId;
    private int fileId;

    public PrepareMessage (String deviceId, int campaignId, int fileId){
        setType(PREPARE);
        setDeviceId(deviceId);
        this.campaignId = campaignId;
        this.fileId = fileId;
    }

    @Override
    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(PREPARE_LENGTH);
        buffer.put(getType());
        buffer.put(getDeviceId().getBytes());
        buffer.putInt(campaignId);
        buffer.putInt(fileId);
        return buffer.array();
    }

    @Override
    public String toString() {
        return PREPARE_STRING
                + " deviceId = " + getDeviceId()
                + " campaignId = " + campaignId
                + " fileId = " + fileId;
    }

    public int getFileId() {
        return fileId;
    }

    public int getCampaignId() {
        return campaignId;
    }
}
