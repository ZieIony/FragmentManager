package pl.zielony.fragmentmanager.test;

import android.view.View;

import com.nineoldandroids.view.ViewHelper;

import pl.zielony.animator.Animator;
import pl.zielony.animator.AnimatorListenerAdapter;
import pl.zielony.animator.UpdateListener;
import pl.zielony.fragmentmanager.Fragment;
import pl.zielony.fragmentmanager.FragmentAnnotation;

/**
 * Created by Marcin on 2015-12-08.
 */
@FragmentAnnotation(layout = R.layout.fragment_detail)
public class DetailFragment extends Fragment {

    @Override
    public Animator animateAdd() {
        final View view = getView().findViewById(R.id.below);
        Animator animator = new Animator();
        animator.setDuration(200);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onStart() {
                ViewHelper.setAlpha(view, 0);
                ViewHelper.setTranslationY(view, view.getHeight() / 2);
            }
        });
        animator.setUpdateListener(new UpdateListener() {
            @Override
            public void onUpdate(float interpolation) {
                ViewHelper.setAlpha(view, interpolation);
                ViewHelper.setTranslationY(view, (1 - interpolation) * view.getHeight() / 2);
            }
        });
        return animator;
    }

    @Override
    public Animator animateRemove() {
        final View view = getView().findViewById(R.id.below);
        Animator animator = new Animator();
        animator.setDuration(200);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onStart() {
                ViewHelper.setAlpha(view, 1);
                ViewHelper.setTranslationY(view, 0);
            }
        });
        animator.setUpdateListener(new UpdateListener() {
            @Override
            public void onUpdate(float interpolation) {
                float fraction = 1 - interpolation;
                ViewHelper.setAlpha(view, fraction);
                ViewHelper.setTranslationY(view, (1 - fraction) * view.getHeight() / 2);
            }
        });
        return animator;
    }
}
