package ee.promobox.promoboxandroid.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

/**
 * Created by ilja on 11.02.2015.
 */
public class TurningRelativeLayout extends RelativeLayout {
    private boolean rotationSet = false;
    private Integer widthMeasureSpec = null;
    private Integer heightMeasureSpec = null;


    public TurningRelativeLayout(Context context) {
        super(context);
    }

    public TurningRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TurningRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TurningRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        Log.d("TurningrelativeLayout", "onmeasure :" + widthMeasureSpec + " heightMeasureSpec= "+heightMeasureSpec);
        if (rotationSet){
            if (this.widthMeasureSpec == null && this.heightMeasureSpec == null) {
                Log.d("TurningrelativeLayout", "integers are NULL");
                this.widthMeasureSpec = widthMeasureSpec;
                this.heightMeasureSpec = heightMeasureSpec;
            }
//            Log.w("TurningrelativeLayout", "onmeasure with rotation :" + this.widthMeasureSpec + " heightMeasureSpec= "+this.heightMeasureSpec);
            super.onMeasure(this.heightMeasureSpec, this.widthMeasureSpec);
            return;
        }
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
    }

    @Override
    public void setRotation(float rotation) {

//        Log.w("TurningrelativeLayout", "setRotation");
        rotationSet = true;
        super.setRotation(rotation);
    }
}
