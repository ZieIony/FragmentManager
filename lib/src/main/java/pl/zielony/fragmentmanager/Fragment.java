package pl.zielony.fragmentmanager;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marcin on 2015-03-20.
 */
public abstract class Fragment implements FragmentManagerInterface {
    private static final String FRAGMENT_MANAGER = "fragmentManager";

    private final Activity activity;
    View view;
    Handler handler;
    private FragmentManager fragmentManager;
    private FragmentManager childFragmentManager;
    private boolean running;

    public Fragment(FragmentManager fragmentManager) {
        this.activity = fragmentManager.getActivity();
        this.fragmentManager = fragmentManager;
        childFragmentManager = new FragmentManager(activity);
        handler = new Handler();
        view = onCreateView();
    }

    protected abstract View onCreateView();

    public void resume() {
        fragmentManager.roots.add(view);
        onResume();
        running = true;
    }

    protected void onResume() {
    }

    public void pause() {
        running = false;
        onPause();
        fragmentManager.roots.remove(view);
    }

    protected void onPause() {
    }

    public boolean isRunning() {
        return running;
    }

    public View getView() {
        return view;
    }

    public Handler getHandler() {
        return handler;
    }

    public Activity getActivity() {
        return activity;
    }

    public Context getContext() {
        return activity;
    }

    public FragmentManager getFragmentManager() {
        return fragmentManager;
    }

    public View findViewById(int id) {
        return view.findViewById(id);
    }

    public List<View> findViewsById(int id) {
        ArrayList<View> result = new ArrayList<>();
        ArrayList<ViewGroup> groups = new ArrayList<>();
        groups.add((ViewGroup) view);

        while (!groups.isEmpty()) {
            ViewGroup group = groups.remove(0);

            for (int i = 0; i < group.getChildCount(); ++i) {
                View child = group.getChildAt(i);
                if (child.getId() == id) {
                    result.add(child);
                }

                if (child instanceof ViewGroup) {
                    groups.add((ViewGroup) child);
                }
            }
        }

        return result;
    }

    public List<View> findViewsWithTag(Object tag) {
        ArrayList<View> result = new ArrayList<>();
        ArrayList<ViewGroup> groups = new ArrayList<>();
        groups.add((ViewGroup) view);

        while (!groups.isEmpty()) {
            ViewGroup group = groups.remove(0);

            for (int i = 0; i < group.getChildCount(); ++i) {
                View child = group.getChildAt(i);
                if (tag.equals(child.getTag())) {
                    result.add(child);
                }

                if (child instanceof ViewGroup) {
                    groups.add((ViewGroup) child);
                }
            }
        }

        return result;
    }

    public String getTitle() {
        return "";
    }

    public void animateInAdd(Animator.AnimatorListener listener) {
        final View view = getView();
        ValueAnimator animator = ValueAnimator.ofFloat(1.1f, 1);
        animator.setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = valueAnimator.getAnimatedFraction();
                float value = (float) valueAnimator.getAnimatedValue();
                ViewHelper.setAlpha(view, fraction);
                ViewHelper.setScaleX(view, value);
                ViewHelper.setScaleY(view, value);
            }
        });
        if (listener != null)
            animator.addListener(listener);
        animator.start();
    }

    public void animateOutAdd(AnimatorListenerAdapter listener) {
        ValueAnimator animator = ValueAnimator.ofFloat(1.1f, 1);
        animator.setDuration(200);
        if (listener != null)
            animator.addListener(listener);
        animator.start();
    }

    public void animateOutBack(Animator.AnimatorListener listener) {
        final View view = getView();
        ValueAnimator animator = ValueAnimator.ofFloat(1, 1.1f);
        animator.setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = 1 - valueAnimator.getAnimatedFraction();
                float value = (float) valueAnimator.getAnimatedValue();
                ViewHelper.setAlpha(view, fraction);
                ViewHelper.setScaleX(view, value);
                ViewHelper.setScaleY(view, value);
            }
        });
        if (listener != null)
            animator.addListener(listener);
        animator.start();
    }

    public void animateInBack() {

    }

    public void onSaveState(Bundle bundle) {
        Bundle fragmentManagerBundle = new Bundle();
        childFragmentManager.save(fragmentManagerBundle);
        bundle.putBundle(FRAGMENT_MANAGER, fragmentManagerBundle);
    }

    public void onRestoreState(Bundle bundle) {
        childFragmentManager.restore(bundle.getBundle(FRAGMENT_MANAGER));
    }

    public String getString(int resId) {
        return activity.getString(resId);
    }

    public String getString(int resId, Object... args) {
        return activity.getString(resId, args);
    }

    public <T extends Fragment> T push(T fragment, final int id) {
        return childFragmentManager.push(fragment, id);
    }

    public <T extends Fragment> T push(T fragment, String tag) {
        return childFragmentManager.push(fragment, tag);
    }

    public <T extends Fragment> T push(Class<T> fragmentClass, final int id) {
        return childFragmentManager.push(fragmentClass, id);
    }

    public <T extends Fragment> T push(Class<T> fragmentClass, String tag) {
        return childFragmentManager.push(fragmentClass, tag);
    }

    public <T extends Fragment> T add(T fragment, int id) {
        return childFragmentManager.add(fragment, id);
    }

    public <T extends Fragment> T add(T fragment, String tag) {
        return childFragmentManager.add(fragment, tag);
    }

    public <T extends Fragment> T add(Class<T> fragmentClass, int id) {
        return childFragmentManager.add(fragmentClass, id);
    }

    public <T extends Fragment> T add(Class<T> fragmentClass, String tag) {
        return childFragmentManager.add(fragmentClass, tag);
    }

    public <T extends Fragment> T join(T fragment, int id) {
        return childFragmentManager.join(fragment, id);
    }

    public <T extends Fragment> T join(T fragment, String tag) {
        return childFragmentManager.join(fragment, tag);
    }

    public <T extends Fragment> T join(Class<T> fragmentClass, int id) {
        return childFragmentManager.join(fragmentClass, id);
    }

    public <T extends Fragment> T join(Class<T> fragmentClass, String tag) {
        return childFragmentManager.join(fragmentClass, tag);
    }

    @Override
    public void back() {
        childFragmentManager.back();
    }

    @Override
    public void up() {
        childFragmentManager.up();
    }

    @Override
    public boolean hasBack() {
        return childFragmentManager.hasBack();
    }

    @Override
    public boolean hasUp() {
        return childFragmentManager.hasUp();
    }
}
