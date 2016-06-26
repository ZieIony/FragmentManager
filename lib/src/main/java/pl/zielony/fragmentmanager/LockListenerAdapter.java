package pl.zielony.fragmentmanager;

import com.nineoldandroids.animation.Animator;

/**
 * Created by Marcin on 2016-06-26.
 */
public class LockListenerAdapter implements Animator.AnimatorListener {
    private FragmentRootView view;

    public LockListenerAdapter(FragmentRootView view) {
        this.view = view;
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        view.setLocked(false);
        animation.removeListener(this);
    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    @Override
    public void onAnimationStart(Animator animation) {
        view.setLocked(true);
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        view.setLocked(false);
        animation.removeListener(this);
    }
}
