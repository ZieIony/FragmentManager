package pl.zielony.animator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marcin on 2016-10-05.
 */

public class AnimatorSet extends Animator {
    List<Animator> animators = new ArrayList<>();

    public AnimatorSet() {
        addListener(new AnimatorListener() {
            @Override
            public void onStart() {
                for (Animator a : animators)
                    a.start();
            }

            @Override
            public void onCancel() {
                for (Animator a : animators)
                    a.cancel();
            }

            @Override
            public void onEnd() {
            }
        });
    }

    public void addAll(List<Animator> animators) {
        this.animators.addAll(animators);
    }
}
