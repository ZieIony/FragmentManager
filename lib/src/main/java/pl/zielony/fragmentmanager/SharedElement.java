package pl.zielony.fragmentmanager;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.List;

/**
 * Created by Marcin on 2016-06-26.
 */
public class SharedElement implements ValueAnimator.AnimatorUpdateListener {
    private static final String FROM = "from";
    private static final String TO = "to";
    private static final String VIEW_ID = "viewId";

    private int idFrom, idTo;
    private int viewId;
    private long duration = DefaultFragmentAnimator.DEFAULT_ANIMATION_DURATION;
    private Interpolator interpolator;

    private View view;
    private FragmentRootView container;
    private KeyFrame frameFrom;
    private KeyFrame frameTo;

    static class KeyFrame {
        Rect rect = new Rect();
    }

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

    private static class TextViewKeyFrame extends KeyFrame {
        float textSize = 0;
        int textColor = 0;
    }

    private static class TextViewAnimatorListener implements ValueAnimator.AnimatorUpdateListener {
        private TextView view;
        private View container;
        private final TextViewKeyFrame frameFrom;
        private final TextViewKeyFrame frameTo;
        ArgbEvaluator evaluator = new ArgbEvaluator();

        TextViewAnimatorListener(TextView view, View container, TextViewKeyFrame frameFrom, TextViewKeyFrame frameTo) {
            this.view = view;
            this.container = container;
            this.frameFrom = frameFrom;
            this.frameTo = frameTo;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float value = (Float) animation.getAnimatedValue();
            Rect rectFrom = frameFrom.rect;
            Rect rectTo = frameTo.rect;
            view.layout(lerp(rectFrom.left, rectTo.left, value),
                    lerp(rectFrom.top, rectTo.top, value),
                    lerp(rectFrom.right, rectTo.right, value),
                    lerp(rectFrom.bottom, rectTo.bottom, value));
            view.setTextColor((Integer) evaluator.evaluate(value, frameFrom.textColor, frameTo.textColor));
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, lerp(frameFrom.textSize, frameTo.textSize, value));
            container.invalidate();
        }
    }

    public SharedElement(View view, Fragment from, Fragment to) {
        this.idFrom = from.getId();
        this.idTo = to.getId();
        this.viewId = view.getId();
    }

    public SharedElement(int id, Fragment from, Fragment to) {
        this.idFrom = from.getId();
        this.idTo = to.getId();
        this.viewId = id;
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

        container = rootTo;

        final int[] containerLocation = new int[2];
        container.getLocationOnScreen(containerLocation);

        final View viewFrom = rootFrom.findViewById(viewId);
        frameFrom = setupFrame(viewFrom, containerLocation);

        final View viewTo = rootTo.findViewById(viewId);
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

    private KeyFrame setupFrame(View view, int[] containerLocation) {
        KeyFrame frame;// = new KeyFrame();
        if (view instanceof TextView) {
            frame = new TextViewKeyFrame();
            ((TextViewKeyFrame) frame).textColor = ((TextView) view).getCurrentTextColor();
            ((TextViewKeyFrame) frame).textSize = ((TextView) view).getTextSize();
        } else {
            frame = new KeyFrame();
        }
        final int[] viewLocation = new int[2];
        view.getLocationOnScreen(viewLocation);
        frame.rect.set(0, 0, view.getWidth(), view.getHeight());
        frame.rect.offset(viewLocation[0] - containerLocation[0], viewLocation[1] - containerLocation[1]);
        return frame;
    }

    private ValueAnimator start(final View viewFrom, final View viewTo) {
        this.view = viewTo;
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(duration);
        if (interpolator == null)
            interpolator = new DecelerateInterpolator();
        animator.setInterpolator(interpolator);
        if (viewFrom instanceof TextView) {
            animator.addUpdateListener(new TextViewAnimatorListener((TextView) view, container, (TextViewKeyFrame) frameFrom, (TextViewKeyFrame) frameTo));
        } else {
            animator.addUpdateListener(this);
        }
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
