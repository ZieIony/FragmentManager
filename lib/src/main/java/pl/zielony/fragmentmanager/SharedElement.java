package pl.zielony.fragmentmanager;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.List;

/**
 * Created by Marcin on 2016-06-26.
 */
public abstract class SharedElement<FrameType extends KeyFrame, ViewType extends View> implements ValueAnimator.AnimatorUpdateListener {
    private static final String FROM = "from";
    private static final String TO = "to";
    private static final String VIEW_ID = "viewId";

    protected int idFrom;
    protected int idTo;
    protected int viewId;
    private long duration = DefaultFragmentAnimator.DEFAULT_ANIMATION_DURATION;
    private Interpolator interpolator;

    protected ViewType view;
    protected FragmentRootView container;
    protected FrameType frameFrom;
    protected FrameType frameTo;

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        float value = (Float) animation.getAnimatedValue();
        Rect rectFrom = frameFrom.rect;
        Rect rectTo = frameTo.rect;
        view.layout(lerp(rectFrom.left, rectTo.left, value),
                lerp(rectFrom.top, rectTo.top, value),
                lerp(rectFrom.right, rectTo.right, value),
                lerp(rectFrom.bottom, rectTo.bottom, value));
        container.invalidate();
    }

    public SharedElement() {
    }

    ValueAnimator start(List<Fragment> fragments, final boolean reverse, final FragmentRootView container) {
        this.container = container;
        Fragment fragmentFrom = null, fragmentTo = null;
        for (Fragment f : fragments) {
            if (f.getId() == idFrom)
                fragmentFrom = f;
            if (f.getId() == idTo)
                fragmentTo = f;
        }

        if (fragmentFrom == null || fragmentTo == null)
            throw new IllegalStateException("SharedElement transition needs two fragments to animate between");

        if (reverse) {
            return start(fragmentTo, fragmentFrom);
        } else {
            return start(fragmentFrom, fragmentTo);
        }
    }

    private ValueAnimator start(Fragment fragmentFrom, Fragment fragmentTo) {
        final FragmentRootView rootFrom = fragmentFrom.getRootView();
        final FragmentRootView rootTo = fragmentTo.getRootView();

        container = rootTo; // TODO: shared element animation type

        final int[] containerLocation = new int[2];
        container.getLocationOnScreen(containerLocation);

        final ViewType viewFrom = (ViewType) rootFrom.findViewById(viewId);
        frameFrom = setupFrame(viewFrom, containerLocation);

        final ViewType viewTo = (ViewType) rootTo.findViewById(viewId);
        frameTo = setupFrame(viewTo, containerLocation);

        ValueAnimator animator = start(viewFrom, viewTo);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                rootFrom.setPreventLayout(true);
                rootTo.setPreventLayout(true);
                viewFrom.setVisibility(View.INVISIBLE);
                viewTo.setVisibility(View.INVISIBLE);
                container.addSharedView(viewTo);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                rootFrom.setPreventLayout(false);
                rootTo.setPreventLayout(false);
                viewFrom.setVisibility(View.VISIBLE);
                viewTo.setVisibility(View.VISIBLE);
                container.removeSharedView(viewTo);
                rootFrom.requestLayout();
                rootTo.requestLayout();
            }
        });

        return animator;
    }

    protected abstract FrameType setupFrame(View view, int[] containerLocation);

    private ValueAnimator start(final ViewType viewFrom, final ViewType viewTo) {
        this.view = viewTo;
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(duration);
        if (interpolator == null)
            interpolator = new DecelerateInterpolator();
        animator.setInterpolator(interpolator);
        animator.addUpdateListener(this);
        return animator;
    }

    protected static int lerp(float a, float b, float t) {
        return (int) (b * t + a * (1 - t));
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setInterpolator(Interpolator interpolator) {
        this.interpolator = interpolator;
    }

    public void save(Bundle sharedElementBundle) {
        sharedElementBundle.putInt(FROM, idFrom);
        sharedElementBundle.putInt(TO, idTo);
        sharedElementBundle.putInt(VIEW_ID, viewId);
    }

    public void restore(Bundle sharedElementBundle) {
        idFrom = sharedElementBundle.getInt(FROM);
        idTo = sharedElementBundle.getInt(TO);
        viewId = sharedElementBundle.getInt(VIEW_ID);
    }
}
