package pl.zielony.fragmentmanager;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.nineoldandroids.view.ViewHelper;

import java.util.List;

/**
 * Created by Marcin on 2016-07-07.
 */

public class FragmentTreeView extends View {
    private FragmentManager fragmentManager;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int step = 20;

    public FragmentTreeView(Context context) {
        super(context);
    }

    public FragmentTreeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FragmentTreeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FragmentTreeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (fragmentManager == null) {
            Log.e(getClass().getSimpleName(), "fragment manager cannot be null!");
            return;
        }
        int y = 20, x = 5;
        paint.setTextSize(14);
        paint.setColor(Color.BLACK);
        paint.setShadowLayer(1, 0, 0, Color.WHITE);
        drawManager(canvas, x, y, fragmentManager);
    }

    private int drawManager(Canvas canvas, int x, int y, ManagerBase manager) {
        List<Fragment> fragments = manager.getFragments();
        for (Fragment f : fragments) {
            View v = f.getView();
            String text = f.getClass().getSimpleName() + " s:" + f.getStateMachine().getState() + " v:" + (f.getRootView().isAttached() && v.getVisibility() == VISIBLE && ViewHelper.getAlpha(v) > 0 ? "t" : "f") + " " + v.getWidth() + "x" + v.getHeight();
            canvas.drawText(text, x, y, paint);
            y += step;
            y = drawManager(canvas, x + step, y, f);
        }
        return y;
    }

}
