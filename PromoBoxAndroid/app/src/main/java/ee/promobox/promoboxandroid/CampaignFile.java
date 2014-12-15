package ee.promobox.promoboxandroid;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by MaximDorofeev on 12.07.2014.
 */
public class CampaignFile implements Comparable<CampaignFile>, Parcelable{
    private int id;
    private String name;
    private CampaignFileType type;
    private int size;
    private String path;

    public CampaignFile() {

    }

    private CampaignFile(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.type = CampaignFileType.valueOf(in.readInt());
        this.size = in.readInt();
        this.setPath(in.readString());
    }

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.id);
        parcel.writeString(this.name);
        parcel.writeInt(this.getType().ordinal());
        parcel.writeInt(this.size);
        parcel.writeString(this.path);
    }

    public static final Parcelable.Creator<CampaignFile> CREATOR = new Parcelable.Creator<CampaignFile>() {
        public CampaignFile createFromParcel(Parcel in) {
            return new CampaignFile(in);
        }

        public CampaignFile[] newArray(int size) {
            return new CampaignFile[size];
        }
    };

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
