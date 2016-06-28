package pl.zielony.fragmentmanager;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.List;

/**
 * Created by Marcin on 2016-06-26.
 */
public class SharedElement {
    private final int idFrom, idTo;
    private int id;
    private Rect rectFrom = new Rect(), rectTo = new Rect();
    private ValueAnimator.AnimatorUpdateListener listener;
    private long duration = 200;
    private Interpolator interpolator;

    public SharedElement(View view, Fragment from, Fragment to) {
        this.idFrom = from.getId();
        this.idTo = to.getId();
        this.id = view.getId();
    }

    public SharedElement(int id, Fragment from, Fragment to) {
        this.idFrom = from.getId();
        this.idTo = to.getId();
        this.id = id;
    }

    void apply(List<Fragment> fragments, final boolean reverse) {
        Fragment fragmentFrom = null, fragmentTo = null;
        for (Fragment f : fragments) {
            if (f.getId() == idFrom)
                fragmentFrom = f;
            if (f.getId() == idTo)
                fragmentTo = f;
        }

        final int[] locationFromFragment = new int[2];
        final int[] locationToFragment = new int[2];
        final int[] locationFromView = new int[2];
        final int[] locationToView = new int[2];
        fragmentFrom.getView().getLocationOnScreen(locationFromFragment);
        fragmentTo.getView().getLocationOnScreen(locationToFragment);
        final View viewFrom = fragmentFrom.getView().findViewById(id);
        final View viewTo = fragmentTo.getView().findViewById(id);
        viewFrom.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                viewFrom.getLocationOnScreen(locationFromView);
                rectFrom.set(0, 0, viewFrom.getWidth(), viewFrom.getHeight());
                rectFrom.offset(locationFromView[0] - locationFromFragment[0], locationFromView[1] - locationFromFragment[1]);
                viewTo.getLocationOnScreen(locationToView);
                rectTo.set(0, 0, viewTo.getWidth(), viewTo.getHeight());
                rectTo.offset(locationToView[0] - locationToFragment[0], locationToView[1] - locationToFragment[1]);
                execute(viewFrom, viewTo, reverse);
                viewFrom.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    private void execute(final View viewFrom, final View viewTo, final boolean reverse) {
        ValueAnimator animator = ValueAnimator.ofFloat(reverse ? 1 : 0, reverse ? 0 : 1);
        animator.setDuration(duration);
        if (interpolator == null)
            interpolator = new DecelerateInterpolator();
        animator.setInterpolator(interpolator);
        if (listener == null) {
            listener = new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (Float) animation.getAnimatedValue();
                    viewTo.layout(lerp(rectFrom.left, rectTo.left, value),
                            lerp(rectFrom.top, rectTo.top, value),
                            lerp(rectFrom.right, rectTo.right, value),
                            lerp(rectFrom.bottom, rectTo.bottom, value));
                }
            };
        }
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (reverse)
                    viewFrom.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                viewFrom.setVisibility(View.INVISIBLE);
            }
        });
        animator.addUpdateListener(listener);
        animator.start();
    }

    private int lerp(float a, float b, float t) {
        return (int) (b * t + a * (1 - t));
    }

    public void setAnimationListener(ValueAnimator.AnimatorUpdateListener listener) {
        this.listener = listener;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setInterpolator(Interpolator interpolator) {
        this.interpolator = interpolator;
    }
}
