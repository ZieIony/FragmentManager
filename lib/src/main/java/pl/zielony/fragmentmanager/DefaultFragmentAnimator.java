package pl.zielony.fragmentmanager;

import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.nineoldandroids.view.ViewHelper;

import pl.zielony.animator.Animator;
import pl.zielony.animator.AnimatorListenerAdapter;
import pl.zielony.animator.UpdateListener;

/**
 * Created by Marcin on 2016-07-07.
 */

public class DefaultFragmentAnimator implements FragmentAnimator {
    public static final int DEFAULT_DURATION = 200;
    private Interpolator decelerateInterpolator = new DecelerateInterpolator();
    private Interpolator accelerateInterpolator = new AccelerateInterpolator();

    public Animator animateAdd(final Fragment fragment) {
        final View view = fragment.getView();
        return new Animator(DEFAULT_DURATION, decelerateInterpolator, new UpdateListener() {
            @Override
            public void onUpdate(float interpolation) {
                ViewHelper.setAlpha(view, interpolation);
                float value = 1.2f - 0.2f * interpolation;
                ViewHelper.setScaleX(view, value);
                ViewHelper.setScaleY(view, value);
            }
        });
    }

    public Animator animateStop(Fragment fragment) {
        Animator animator = new Animator();
        animator.setDuration(DEFAULT_DURATION);
        return animator;
    }

    public Animator animateRemove(final Fragment fragment) {
        final View view = fragment.getView();
        return new Animator(DEFAULT_DURATION, accelerateInterpolator, new UpdateListener() {
            @Override
            public void onUpdate(float interpolation) {
                ViewHelper.setAlpha(view, 1 - interpolation);
                float value = 1.0f + 0.2f * interpolation;
                ViewHelper.setScaleX(view, value);
                ViewHelper.setScaleY(view, value);
            }
        });
    }

    public Animator animateStart(Fragment fragment) {
        final View view = fragment.getView();
        Animator animator = new Animator();
        animator.setDuration(DEFAULT_DURATION);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onStart() {
                ViewHelper.setAlpha(view, 1);
                ViewHelper.setScaleX(view, 1);
                ViewHelper.setScaleY(view, 1);
            }
        });
        return animator;
    }

}
