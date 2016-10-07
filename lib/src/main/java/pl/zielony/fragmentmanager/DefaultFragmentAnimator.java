package pl.zielony.fragmentmanager;

import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.nineoldandroids.view.ViewHelper;

import pl.zielony.animator.Animator;
import pl.zielony.animator.AnimatorListenerAdapter;
import pl.zielony.animator.UpdateListener;

/**
 * Created by Marcin on 2016-07-07.
 */

public class DefaultFragmentAnimator implements FragmentAnimator {
    public static final int DEFAULT_ANIMATION_DURATION = 2000;

    public Animator animateAdd(final Fragment fragment) {
        final View view = fragment.getView();
        Animator animator = new Animator();
        animator.setDuration(DEFAULT_ANIMATION_DURATION);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setUpdateListener(new UpdateListener() {
            @Override
            public void onUpdate(float interpolation) {
                ViewHelper.setAlpha(view, interpolation);
                float value = 1.2f - 0.2f * interpolation;
                ViewHelper.setScaleX(view, value);
                ViewHelper.setScaleY(view, value);
            }
        });
        return animator;
    }

    public Animator animateStop(Fragment fragment) {
        Animator animator = new Animator();
        animator.setDuration(DEFAULT_ANIMATION_DURATION);
        animator.setInterpolator(new DecelerateInterpolator());
        return animator;
    }

    public Animator animateRemove(final Fragment fragment) {
        final View view = fragment.getView();
        Animator animator = new Animator();
        animator.setDuration(DEFAULT_ANIMATION_DURATION);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setUpdateListener(new UpdateListener() {
            @Override
            public void onUpdate(float interpolation) {
                ViewHelper.setAlpha(view, 1 - interpolation);
                float value = 1.0f + 0.2f * interpolation;
                ViewHelper.setScaleX(view, value);
                ViewHelper.setScaleY(view, value);
            }
        });
        return animator;
    }

    public Animator animateStart(Fragment fragment) {
        final View view = fragment.getView();
        Animator animator = new Animator();
        animator.setDuration(DEFAULT_ANIMATION_DURATION);
        animator.setInterpolator(new DecelerateInterpolator());
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
