package labs.module08309.acw;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ViewFlipper;

import java.io.Serializable;

/**
 * Created by Toby on 04/05/2016.
 */
public class MyViewFlipper extends ViewFlipper implements Serializable{
    public MyViewFlipper(Context context) {
        super(context);
    }

    //This class exists to resolve a documented bug with the android api

    public MyViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDetachedFromWindow() {
        try{
            super.onDetachedFromWindow();
        }catch(Exception ignored) {
        }
    }
}
