package pl.zielony.fragmentmanager;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Marcin on 2016-05-17.
 */
public class FragmentRootView extends FrameLayout {
    private boolean locked = false;
    private Map<View, ViewDesc> sharedViews = new HashMap<>();

    private List<OnLayoutChangeListener> layoutListeners = new ArrayList<>();
    private List<OnAttachStateChangeListener> attachListeners = new ArrayList<>();
    private boolean preventLayout = false;

    private boolean attached = false;

    class ViewDesc {
        private final int left, right, top, bottom;

        public ViewDesc(View view) {
            left = view.getLeft();
            top = view.getTop();
            right = view.getRight();
            bottom = view.getBottom();
        }

        void restore(View view) {
            view.layout(left, top, right, bottom);
        }
    }

    public interface OnLayoutChangeListener {
        void onLayoutChange(View v, int left, int top, int right, int bottom,
                            int oldLeft, int oldTop, int oldRight, int oldBottom);
    }

    public interface OnAttachStateChangeListener {
        void onViewAttachedToWindow(View v);

        void onViewDetachedFromWindow(View v);
    }

    public FragmentRootView(Context context) {
        super(context);
        setBackgroundColor(Color.RED);
    }

    public FragmentRootView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.RED);
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

    public boolean isAttached() {
        return attached;
    }

    @Override
    protected void onAttachedToWindow() {
        attached = true;
        super.onAttachedToWindow();
        for (OnAttachStateChangeListener listener : attachListeners)
            listener.onViewAttachedToWindow(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        attached = false;
        super.onDetachedFromWindow();
        for (OnAttachStateChangeListener listener : attachListeners)
            listener.onViewDetachedFromWindow(this);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int prevLeft = getLeft();
        int prevTop = getTop();
        int prevRight = getRight();
        int prevBottom = getBottom();
        super.onLayout(changed, left, top, right, bottom);
        for (OnLayoutChangeListener listener : layoutListeners)
            listener.onLayoutChange(this, left, top, right, bottom, prevLeft, prevTop, prevRight, prevBottom);
    }

    @Override
    public void requestLayout() {
        if (preventLayout)
            return;
        super.requestLayout();
    }

    public void addOnLayoutChangeListener(OnLayoutChangeListener listener) {
        layoutListeners.add(listener);
    }

    public void removeOnLayoutChangeListener(OnLayoutChangeListener listener) {
        layoutListeners.remove(listener);
    }

    public void addOnAttachStateChangeListener(OnAttachStateChangeListener listener) {
        attachListeners.add(listener);
    }

    public void removeOnAttachStateChangeListener(OnAttachStateChangeListener listener) {
        attachListeners.remove(listener);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return locked ? true : super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return locked ? true : super.dispatchKeyEvent(event);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        for (View v : sharedViews.keySet())
            drawChild(canvas, v, getDrawingTime());
    }

    public void addSharedView(View view) {
        sharedViews.put(view, new ViewDesc(view));
    }

    public void removeSharedView(View view) {
        sharedViews.remove(view).restore(view);
    }

    public boolean isPreventLayout() {
        return preventLayout;
    }

    public void setPreventLayout(boolean preventLayout) {
        this.preventLayout = preventLayout;
    }
}
