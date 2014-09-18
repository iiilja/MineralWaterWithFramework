package ee.promobox.promoboxandroid;

/**
 * Created by MaximDorofeev on 12.07.2014.
 */
public class CampaignFile implements Comparable<CampaignFile>{
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

    @Override
    public int compareTo(CampaignFile campaignFile) {
        if (this.getId() > campaignFile.getId()) {
            return 1;
        } else if (this.getId() < campaignFile.getId()){
            return -1;
        }

        return 0;
    }
}
