package ee.promobox.promoboxandroid.data;

/**
 * Created by ilja on 6.05.2015.
 */
public enum AudioOut {
    AUDIO_HDMI, AUDIO_SPDIF, AUDIO_CODEC;

    public static String getByOutNumber(int outNumber) {
        switch (outNumber) {
            case 1:
                return AUDIO_HDMI.name();
            case 2:
                return AUDIO_SPDIF.name();
            default:
            case 0:
                return AUDIO_CODEC.name();
        }
    }
}
