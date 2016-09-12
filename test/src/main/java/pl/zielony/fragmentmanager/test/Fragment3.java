package pl.zielony.fragmentmanager.test;

import android.view.View;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

import pl.zielony.fragmentmanager.Fragment;
import pl.zielony.fragmentmanager.FragmentAnnotation;

/**
 * Created by Marcin on 2015-12-08.
 */
@FragmentAnnotation(layout = R.layout.fragment3)
public class Fragment3 extends Fragment {

    @Override
    public Animator animateAdd() {
        final View view = getView().findViewById(R.id.below);
        ValueAnimator animator = ValueAnimator.ofFloat(1.1f, 1);
        animator.setDuration(200);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                ViewHelper.setAlpha(view, 0);
                ViewHelper.setTranslationY(view, view.getHeight() / 2);
            }
        });
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = valueAnimator.getAnimatedFraction();
                ViewHelper.setAlpha(view, fraction);
                ViewHelper.setTranslationY(view, (1 - fraction) * view.getHeight() / 2);
            }
        });
        return animator;
    }

    @Override
    public ValueAnimator animateRemove() {
        final View view = getView().findViewById(R.id.below);
        ValueAnimator animator = ValueAnimator.ofFloat(1, 1.1f);
        animator.setDuration(200);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                ViewHelper.setAlpha(view, 1);
                ViewHelper.setTranslationY(view, 0);
            }
        });
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = 1 - valueAnimator.getAnimatedFraction();
                ViewHelper.setAlpha(view, fraction);
                ViewHelper.setTranslationY(view, (1 - fraction) * view.getHeight() / 2);
            }
        });
        return animator;
    }
}
