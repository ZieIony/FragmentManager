package pl.zielony.fragmentmanager;

import android.view.View;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

/**
 * Created by Marcin on 2016-07-07.
 */

public class DefaultFragmentAnimator extends FragmentAnimator {

    public Animator animateAdd(Fragment fragment) {
        final View view = fragment.getView();
        ValueAnimator animator = ValueAnimator.ofFloat(1.1f, 1);
        animator.setDuration(200);
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
        animator.start();
        return animator;
    }

    public Animator animateStop(Fragment fragment) {
        ValueAnimator animator = ValueAnimator.ofFloat(1.1f, 1);
        animator.setDuration(200);
        animator.start();
        return animator;
    }

    public Animator animateRemove(Fragment fragment) {
        final View view = fragment.getView();
        ValueAnimator animator = ValueAnimator.ofFloat(1, 1.1f);
        animator.setDuration(200);
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
        animator.start();
        return animator;
    }

    public Animator animateStart(Fragment fragment) {
        return null;
    }

}
