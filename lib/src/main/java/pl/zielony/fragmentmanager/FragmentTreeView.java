package pl.zielony.fragmentmanager;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

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
        int y = 20, x = 5;
        paint.setTextSize(14);
        paint.setColor(Color.BLACK);
        drawManager(canvas, y, x, fragmentManager);
    }

    private void drawManager(Canvas canvas, int y, int x, FragmentManager manager) {
        List<Fragment> fragments = manager.getFragments();
        for (Fragment f : fragments) {
            canvas.drawText(f.getClass().getSimpleName(), x, y, paint);
            y += step;
            drawManager(canvas, y, x + step, f.getChildFragmentManager());
        }
    }

}
