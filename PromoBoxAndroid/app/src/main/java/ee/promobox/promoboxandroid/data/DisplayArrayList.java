package ee.promobox.promoboxandroid.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ilja on 6.03.2015.
 */
public class DisplayArrayList extends ArrayList<Display> {

    public DisplayArrayList(List<Display> displays) {
        super(displays);
    }

    public DisplayArrayList() {

    }

    public Display getDisplayWithId(int id) {
        for (Display d : this){
            if (d.getId() == id) return d;
        }
        return null;
    }
}
