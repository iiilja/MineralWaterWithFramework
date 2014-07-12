package ee.promobox.promoboxandroid;

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
        for (CampaignFileType ctype : CampaignFileType.values()) {
            if (ctype.type == type) return ctype;
        }

        return  CampaignFileType.IMAGE;
    }
}
