package pl.zielony.fragmentmanager;

import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.List;

/**
 * Created by Marcin on 2016-06-26.
 */
class SharedElement {
    private final int from, to;
    private int id;
    private int left, top, right, bottom;
    private int left2, top2, right2, bottom2;
    private ValueAnimator.AnimatorUpdateListener listener;

    SharedElement(View view, Fragment from, Fragment to) {
        this.from = from.getId();
        this.to = to.getId();
        this.id = view.getId();
    }

    SharedElement(int id, Fragment from, Fragment to) {
        this.from = from.getId();
        this.to = to.getId();
        this.id = id;
    }

    void apply(List<Fragment> fragments, final boolean reverse) {
        Fragment fragmentFrom = null, fragmentTo = null;
        for (Fragment f : fragments) {
            if (f.getId() == from)
                fragmentFrom = f;
            if (f.getId() == to)
                fragmentTo = f;
        }

        final View viewFrom = fragmentFrom.getView().findViewById(id);
        final View viewTo = fragmentTo.getView().findViewById(id);
        viewFrom.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                left = viewFrom.getLeft();
                top = viewFrom.getTop();
                right = viewFrom.getRight();
                bottom = viewFrom.getBottom();
                left2 = viewTo.getLeft();
                top2 = viewTo.getTop();
                right2 = viewTo.getRight();
                bottom2 = viewTo.getBottom();
                execute(viewTo, reverse);
                viewFrom.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    private void execute(final View view, boolean reverse) {
        ValueAnimator animator = ValueAnimator.ofFloat(reverse ? 1 : 0, reverse ? 0 : 1);
        animator.setDuration(200);
        animator.setInterpolator(new DecelerateInterpolator());
        if (listener != null) {
            animator.addUpdateListener(listener);
        } else {
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (Float) animation.getAnimatedValue();
                    view.layout((int) lerp(left, left2, value),
                            (int) lerp(top, top2, value),
                            (int) lerp(right, right2, value),
                            (int) lerp(bottom, bottom2, value));
                }
            });
        }
        animator.start();
    }

    private float lerp(float a, float b, float t) {
        return b * t + a * (1 - t);
    }

    public void setAnimationListener(ValueAnimator.AnimatorUpdateListener listener) {
        this.listener = listener;
    }
}
