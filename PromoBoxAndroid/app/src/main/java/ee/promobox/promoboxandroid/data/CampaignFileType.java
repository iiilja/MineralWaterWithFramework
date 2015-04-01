package ee.promobox.promoboxandroid.data;

/**
 * Created by MaximDorofeev on 12.07.2014.
 */
public enum CampaignFileType {
    IMAGE(1), AUDIO(2), VIDEO(3), HTML(4), SWF(5);

    private int type;

    private  CampaignFileType(int type) {
        this.type = type;
    }

    public static CampaignFileType valueOf(int type) {
        for (CampaignFileType cType : CampaignFileType.values()) {
            if (cType.type == type) return cType;
        }

        return  CampaignFileType.IMAGE;
    }
}
