package ee.promobox.promoboxandroid.data;

import java.util.ArrayList;

/**
 * Created by ilja on 6.03.2015.
 */
public class DisplayArrayList extends ArrayList<Display> {

    public Display getDisplayWithId(int id) {
        for (Display d : this){
            if (d.getId() == id) return d;
        }
        return null;
    }
}
