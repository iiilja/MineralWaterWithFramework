package ee.promobox.promoboxandroid.widgets;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by ilja on 9.02.2015.
 */
public class RoboticTextView extends TextView{
    public RoboticTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public RoboticTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RoboticTextView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        Typeface tf = Typeface.createFromAsset(context.getAssets(),
                "fonts/Roboto-Medium.ttf");
        setTypeface(tf);
    }
}
