package pl.zielony.fragmentmanager;

import pl.zielony.animator.Animator;

/**
 * Created by Marcin on 2016-10-09.
 */

public class EmptyFragmentAnimator implements FragmentAnimator {
    @Override
    public Animator animateAdd(Fragment fragment) {
        return null;
    }

    @Override
    public Animator animateStart(Fragment fragment) {
        return null;
    }

    @Override
    public Animator animateStop(Fragment fragment) {
        return null;
    }

    @Override
    public Animator animateRemove(Fragment fragment) {
        return null;
    }
}
