package ee.promobox.promoboxandroid.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by ilja on 28.01.2015.
 */
public class ErrorMessage implements Parcelable{
    private String name;
    private String message;
    private String stackTrace;
    private long date;

    public ErrorMessage(Parcel in) {
        name = in.readString();
        date = in.readLong();
        message = in.readString();
        stackTrace = in.readString();
    }

    public ErrorMessage(String name, String message, StackTraceElement[] stackTrace){
        this.name = name;
        this.date = System.currentTimeMillis();
        this.message = message;
        this.stackTrace = stackTraceToString(stackTrace);
    }

    public ErrorMessage(Exception ex){
        this(ex.getClass().getSimpleName(),ex.getLocalizedMessage(),ex.getStackTrace());
    }

    public void putMoreInfo(String someInfo){
        stackTrace += " My additional info:\t" + someInfo;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String stackTraceToString(StackTraceElement[] stackTrace){
        if (stackTrace == null ) return "";

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < stackTrace.length; i++) {
            StackTraceElement element = stackTrace[i];
            builder.append(element.toString());
        }
        return builder.toString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeLong(date);
        parcel.writeString(message);
        parcel.writeString(stackTrace);
    }

    public static final Parcelable.Creator<ErrorMessage> CREATOR = new Parcelable.Creator<ErrorMessage>() {
        public ErrorMessage createFromParcel(Parcel in) {
            return new ErrorMessage(in);
        }

        public ErrorMessage[] newArray(int size) {
            return new ErrorMessage[size];
        }
    };
}
