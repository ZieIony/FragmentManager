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

public class ModalFragmentAnimator implements FragmentAnimator {
    public static final int DEFAULT_DURATION = 200;
    private Interpolator decelerateInterpolator = new DecelerateInterpolator();
    private Interpolator accelerateInterpolator = new AccelerateInterpolator();

    public Animator animateAdd(final Fragment fragment) {
        final View view = fragment.getView();
        return new Animator(DEFAULT_DURATION, decelerateInterpolator, new UpdateListener() {
            @Override
            public void onUpdate(float interpolation) {
                ViewHelper.setAlpha(view, interpolation);
                float value = view.getHeight() * (1.0f - interpolation) / 2.0f;
                ViewHelper.setTranslationY(view, value);
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
                float value = view.getHeight() * interpolation / 2.0f;
                ViewHelper.setTranslationY(view, value);
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
                ViewHelper.setTranslationY(view, 0);
            }
        });
        return animator;
    }

}
