package pl.zielony.fragmentmanager;

import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

/**
 * Created by Marcin on 2016-07-07.
 */

public class DefaultFragmentAnimator implements FragmentAnimator {
    public static final int DEFAULT_ANIMATION_DURATION = 200;

    public Animator animateAdd(Fragment fragment) {
        final View view = fragment.getView();
        ValueAnimator animator = ValueAnimator.ofFloat(1.1f, 1);
        animator.setDuration(DEFAULT_ANIMATION_DURATION);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = valueAnimator.getAnimatedFraction();
                float value = (float) valueAnimator.getAnimatedValue();
                ViewHelper.setAlpha(view, fraction);
                ViewHelper.setScaleX(view, value);
                ViewHelper.setScaleY(view, value);
            }
        });
        return null;
    }

    public Animator animateStop(Fragment fragment) {
        ValueAnimator animator = ValueAnimator.ofFloat(1.1f, 1);
        animator.setDuration(DEFAULT_ANIMATION_DURATION);
        animator.setInterpolator(new DecelerateInterpolator());
        return null;
    }

    public Animator animateRemove(Fragment fragment) {
        final View view = fragment.getView();
        ValueAnimator animator = ValueAnimator.ofFloat(1, 1.1f);
        animator.setDuration(DEFAULT_ANIMATION_DURATION);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = 1 - valueAnimator.getAnimatedFraction();
                float value = (float) valueAnimator.getAnimatedValue();
                ViewHelper.setAlpha(view, fraction);
                ViewHelper.setScaleX(view, value);
                ViewHelper.setScaleY(view, value);
            }
        });
        return null;
    }

    public Animator animateStart(Fragment fragment) {
        final View view = fragment.getView();
        ViewHelper.setAlpha(view, 1);
        ViewHelper.setScaleX(view, 1);
        ViewHelper.setScaleY(view, 1);
        return null;
    }

}
