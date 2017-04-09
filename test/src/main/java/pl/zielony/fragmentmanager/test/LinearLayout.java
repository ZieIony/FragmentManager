package pl.zielony.fragmentmanager.test;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;

import com.nineoldandroids.view.ViewHelper;

/**
 * Created by Marcin on 2017-01-11.
 */

public class LinearLayout extends carbon.widget.LinearLayout {
    public LinearLayout(Context context) {
        super(context);
    }

    public LinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        Log.e("linearlayout", hashCode()+" v: " + getVisibility() + ", a: " + ViewHelper.getAlpha(this));
        super.dispatchDraw(canvas);
    }
}
