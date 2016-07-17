package pl.zielony.fragmentmanager;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by Marcin on 2016-05-17.
 */
public class FragmentRootView extends FrameLayout {
    private boolean locked = false;

    public interface OnLayoutChangeListener {
        /**
         * Called when the layout bounds of a view changes due to layout processing.
         *
         * @param v         The view whose bounds have changed.
         * @param left      The new value of the view's left property.
         * @param top       The new value of the view's top property.
         * @param right     The new value of the view's right property.
         * @param bottom    The new value of the view's bottom property.
         * @param oldLeft   The previous value of the view's left property.
         * @param oldTop    The previous value of the view's top property.
         * @param oldRight  The previous value of the view's right property.
         * @param oldBottom The previous value of the view's bottom property.
         */
        void onLayoutChange(View v, int left, int top, int right, int bottom,
                            int oldLeft, int oldTop, int oldRight, int oldBottom);
    }

    private OnLayoutChangeListener listener;

    public FragmentRootView(Context context) {
        super(context);
    }

    public FragmentRootView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FragmentRootView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FragmentRootView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int prevLeft = getLeft();
        int prevTop = getTop();
        int prevRight = getRight();
        int prevBottom = getBottom();
        super.onLayout(changed, left, top, right, bottom);
        if (changed && listener != null)
            listener.onLayoutChange(this, left, top, right, bottom, prevLeft, prevTop, prevRight, prevBottom);
    }

    public void addOnLayoutChangeListener(OnLayoutChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return locked ? true : super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return locked ? true : super.dispatchKeyEvent(event);
    }
}
