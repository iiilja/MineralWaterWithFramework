package ee.promobox.promoboxandroid.data;

import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ilja on 5.03.2015.
 */
public class Display implements Parcelable, Comparable<Display>{
    private Point[] points = new Point[4];
    private int id;

    public Display(int id, Point topLeft, Point topRight, Point bottomRight, Point bottomLeft){
        this.id = id;

        points[0] = topLeft;
        points[1] = topRight;
        points[2] = bottomRight;
        points[3] = bottomLeft;
    }

    private Display(Parcel in) {
        id = in.readInt();
        points[0] = new Point(in.readInt(),in.readInt());
        points[1] = new Point(in.readInt(),in.readInt());
        points[2] = new Point(in.readInt(),in.readInt());
        points[3] = new Point(in.readInt(),in.readInt());
    }

    public Display(JSONObject display) throws JSONException{
        if (display.has("displayId")
                && display.has("point1")
                && display.has("point2")
                && display.has("point3")
                && display.has("point4") ) {
            id = display.getInt("displayId");
            points[0] = getPointFromJSON(display.getString("point1"));
            points[1] = getPointFromJSON(display.getString("point2"));
            points[2] = getPointFromJSON(display.getString("point3"));
            points[3] = getPointFromJSON(display.getString("point4"));
        } else {
            throw new JSONException("Something missing in display JSON");
        }
    }

    private Point getPointFromJSON(String point){
        String[] points = point.split(",");
        return new Point(Integer.parseInt(points[0]),Integer.parseInt(points[1]));
    }

    public static final Parcelable.Creator<Display> CREATOR = new Parcelable.Creator<Display>() {
        public Display createFromParcel(Parcel in) {
            return new Display(in);
        }

        public Display[] newArray(int size) {
            return new Display[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * points[0] = topLeft;
     * points[1] = topRight;
     * points[2] = bottomRight;
     * points[3] = bottomLeft;
     */
    public Point[] getPoints() {
        return points;
    }

    public int getId() {
        return id;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.id);
        out.writeInt(points[0].x);
        out.writeInt(points[0].y);
        out.writeInt(points[1].x);
        out.writeInt(points[1].y);
        out.writeInt(points[2].x);
        out.writeInt(points[2].y);
        out.writeInt(points[3].x);
        out.writeInt(points[3].y);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this ) return true;
        if (o == null || o.getClass() != this.getClass()) return false;
        Display d = (Display) o;
        return (d.id == this.id
                && d.points[0].equals(points[0])
                && d.points[1].equals(points[1])
                && d.points[2].equals(points[2])
                && d.points[3].equals(points[3]));
    }

    @Override
    public int compareTo(Display another) {
        if (id > another.id){
            return 1;
        } else if (id < another.id){
            return -1;
        } else {
            return 0;
        }
    }
}
