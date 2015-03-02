package ee.promobox.promoboxandroid.util.udp_multicasting.messages;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Created by ilja on 25.02.2015.
 */
public class PlayMessage extends MultiCastMessage {
    private int campaignId;
    private int fileId;
    private long frameId;

    public PlayMessage (String deviceId, int campaignId, int fileId, long frameId){
        setType(PLAY);
        setDeviceId(deviceId);
        this.campaignId = campaignId;
        this.fileId = fileId;
        this.frameId = frameId;
    }

    @Override
    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(PLAY_LENGTH);
        buffer.put(getType());
        byte[] bytes = getDeviceId().getBytes(Charset.forName("UTF-8"));
        buffer.put(getDeviceId().getBytes(Charset.forName("UTF-8")));
        buffer.putInt(campaignId);
        buffer.putInt(fileId);
        buffer.putLong(frameId);
        return buffer.array();
    }

    @Override
    public String toString() {
        return PLAY_STRING
                + " deviceId = " + getDeviceId()
                + " campaignId = " + campaignId
                + " fileId = " + fileId
                + " frameId = " + frameId;
    }

    public int getCampaignId() {
        return campaignId;
    }

    public int getFileId() {
        return fileId;
    }

    public long getFrameId() {
        return frameId;
    }
}
