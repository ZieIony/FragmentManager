package pl.zielony.fragmentmanager.test;

import android.view.View;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

import pl.zielony.fragmentmanager.Fragment;
import pl.zielony.fragmentmanager.FragmentManager;
import pl.zielony.fragmentmanager.FragmentRootView;
import pl.zielony.fragmentmanager.LockListenerAdapter;

/**
 * Created by Marcin on 2015-12-08.
 */
public class Fragment3 extends Fragment {

    @Override
    protected int getViewResId() {
        return R.layout.fragment3;
    }

    @Override
    public Animator animateStart() {
        final View view = getView().findViewById(R.id.below);
        ViewHelper.setAlpha(view, 0);
        ValueAnimator animator = ValueAnimator.ofFloat(1.1f, 1);
        animator.setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = valueAnimator.getAnimatedFraction();
                ViewHelper.setAlpha(view, fraction);
                ViewHelper.setTranslationY(view, (1 - fraction) * view.getHeight() / 2);
            }
        });
        if (view instanceof FragmentRootView)
            animator.addListener(new LockListenerAdapter((FragmentRootView) view));
        animator.start();
        return animator;
    }

    @Override
    public ValueAnimator animateFinish() {
        final View view = getView().findViewById(R.id.below);
        ValueAnimator animator = ValueAnimator.ofFloat(1, 1.1f);
        animator.setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = 1 - valueAnimator.getAnimatedFraction();
                ViewHelper.setAlpha(view, fraction);
                ViewHelper.setTranslationY(view, (1 - fraction) * view.getHeight() / 2);
            }
        });
        if (view instanceof FragmentRootView)
            animator.addListener(new LockListenerAdapter((FragmentRootView) view));
        animator.start();
        return animator;
    }
}
