package ee.promobox.promoboxandroid;

/**
 * Created by MaximDorofeev on 12.07.2014.
 */
public class CampaignFile {
    private String name;
    private CampaignFileType type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CampaignFileType getType() {
        return type;
    }

    public void setType(CampaignFileType type) {
        this.type = type;
    }
}
