package ee.promobox.promoboxandroid;

/**
 * Created by MaximDorofeev on 12.07.2014.
 */
public class CampaignFile {
    private int id;
    private String name;
    private CampaignFileType type;
    private int size;

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

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
