package pl.zielony.fragmentmanager;

import pl.zielony.animator.Animator;

/**
 * Created by Marcin on 2016-07-07.
 */

public interface FragmentAnimator {
    Animator animateAdd(Fragment fragment);

    Animator animateStart(Fragment fragment);

    Animator animateStop(Fragment fragment);

    Animator animateRemove(Fragment fragment);
}
