package pl.zielony.fragmentmanager;

import com.nineoldandroids.animation.Animator;

/**
 * Created by Marcin on 2016-07-07.
 */

public abstract class FragmentAnimator {
    public abstract Animator animateAdd(Fragment fragment);

    public abstract Animator animateStart(Fragment fragment);

    public abstract Animator animateStop(Fragment fragment);

    public abstract Animator animateRemove(Fragment fragment);
}
