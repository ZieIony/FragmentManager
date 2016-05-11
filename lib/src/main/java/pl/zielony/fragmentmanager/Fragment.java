package pl.zielony.fragmentmanager;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.SparseArray;
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
    private static final String HIERARCHY_STATE = "hierarchyState";
    static final String TARGET = "target";
    static final String ID = "id";

    private final Activity activity;
    View view;
    Handler handler;
    private FragmentManager fragmentManager;
    private FragmentManager childFragmentManager;
    private boolean running;
    Fragment parent;
    Integer target;
    private Bundle result;
    private int id;
    static int idSequence = 0;

    public Fragment(FragmentManager fragmentManager) {
        this.activity = fragmentManager.getActivity();
        this.fragmentManager = fragmentManager;
        handler = new Handler();
        view = onCreateView();
        childFragmentManager = new FragmentManager(fragmentManager);
        id = idSequence++;
    }

    protected abstract View onCreateView();

    public void start(){
        onStart();
        running = true;
    }

    public void resume() {
        onResume();
        running = true;
    }

    protected void onResume() {
    }

    public void finish(){
        running = false;
        onFinish();
    }

    public void pause() {
        running = false;
        onPause();
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

    public FragmentManager getChildFragmentManager() {
        return childFragmentManager;
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

    public void animateInAdd() {
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

        SparseArray<Parcelable> container = new SparseArray<>();
        view.saveHierarchyState(container);
        bundle.putSparseParcelableArray(HIERARCHY_STATE, container);

        if (target != null)
            bundle.putInt(TARGET, target);
        bundle.putInt(ID, id);
    }

    public void onRestoreState(Bundle bundle) {
        view.restoreHierarchyState(bundle.getSparseParcelableArray(HIERARCHY_STATE));
        childFragmentManager.restore(bundle.getBundle(FRAGMENT_MANAGER));

        if (bundle.containsKey(TARGET))
            target = bundle.getInt(TARGET);
        id = bundle.getInt(ID);
    }

    public void onResult(Bundle result) {
    }

    public void setTargetFragment(Fragment target) {
        this.target = target.id;
    }

    public void setResult(Bundle result) {
        if (target == null)
            return;
        fragmentManager.setResult(target, result);
    }

    public String getString(int resId) {
        return activity.getString(resId);
    }

    public String getString(int resId, Object... args) {
        return activity.getString(resId, args);
    }

    public <T extends Fragment> T add(T fragment, int id) {
        T f = childFragmentManager.add(fragment, id);
        f.setParent(this);
        return f;
    }

    public <T extends Fragment> T add(T fragment, String tag) {
        T f = childFragmentManager.add(fragment, tag);
        f.setParent(this);
        return f;
    }

    public <T extends Fragment> T add(Class<T> fragmentClass, int id) {
        T f = childFragmentManager.add(fragmentClass, id);
        f.setParent(this);
        return f;
    }

    public <T extends Fragment> T add(Class<T> fragmentClass, String tag) {
        T f = childFragmentManager.add(fragmentClass, tag);
        f.setParent(this);
        return f;
    }

    public <T extends Fragment> T push(T fragment, int id) {
        T f = childFragmentManager.push(fragment, id);
        f.setParent(this);
        return f;
    }

    public <T extends Fragment> T push(T fragment, String tag) {
        T f = childFragmentManager.push(fragment, tag);
        f.setParent(this);
        return f;
    }

    public <T extends Fragment> T push(Class<T> fragmentClass, int id) {
        T f = childFragmentManager.push(fragmentClass, id);
        f.setParent(this);
        return f;
    }

    public <T extends Fragment> T push(Class<T> fragmentClass, String tag) {
        T f = childFragmentManager.push(fragmentClass, tag);
        f.setParent(this);
        return f;
    }

    public <T extends Fragment> T join(T fragment, int id) {
        T f = childFragmentManager.join(fragment, id);
        f.setParent(this);
        return f;
    }

    public <T extends Fragment> T join(T fragment, String tag) {
        T f = childFragmentManager.join(fragment, tag);
        f.setParent(this);
        return f;
    }

    public <T extends Fragment> T join(Class<T> fragmentClass, int id) {
        T f = childFragmentManager.join(fragmentClass, id);
        f.setParent(this);
        return f;
    }

    public <T extends Fragment> T join(Class<T> fragmentClass, String tag) {
        T f = childFragmentManager.join(fragmentClass, tag);
        f.setParent(this);
        return f;
    }

    public <T extends Fragment> T dialog(T fragment, int id) {
        T f = childFragmentManager.dialog(fragment, id);
        f.setParent(this);
        return f;
    }

    public <T extends Fragment> T dialog(T fragment, String tag) {
        T f = childFragmentManager.dialog(fragment, tag);
        f.setParent(this);
        return f;
    }

    public <T extends Fragment> T dialog(Class<T> fragmentClass, int id) {
        T f = childFragmentManager.dialog(fragmentClass, id);
        f.setParent(this);
        return f;
    }

    public <T extends Fragment> T dialog(Class<T> fragmentClass, String tag) {
        T f = childFragmentManager.dialog(fragmentClass, tag);
        f.setParent(this);
        return f;
    }

    @Override
    public boolean back() {
        return childFragmentManager.back();
    }

    @Override
    public boolean up() {
        return childFragmentManager.up();
    }

    @Override
    public boolean hasBack() {
        return childFragmentManager.hasBack();
    }

    @Override
    public boolean hasUp() {
        return childFragmentManager.hasUp();
    }

    public Fragment getParent() {
        return parent;
    }

    public void setParent(Fragment parent) {
        this.parent = parent;
    }

    public int getId() {
        return id;
    }

    protected void onFinish() {

    }

    protected void onStart() {

    }
}
