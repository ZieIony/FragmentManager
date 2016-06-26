package pl.zielony.fragmentmanager;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
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
    static final String TAG = "tag";

    private final Activity activity;
    private View view;
    private Handler handler;
    private FragmentManager fragmentManager;
    private FragmentManager childFragmentManager;
    private boolean running;
    private Fragment parent;
    private Integer target;
    private int id;
    static int idSequence = 0;
    private String tag;

    public Fragment(FragmentManager fragmentManager) {
        this.activity = fragmentManager.getActivity();
        this.fragmentManager = fragmentManager;
        handler = new Handler();
        view = onCreateView();
        childFragmentManager = new FragmentManager(fragmentManager);
        id = idSequence++;
    }

    protected View onCreateView() {
        return View.inflate(getContext(), getViewResId(), null);
    }

    protected int getViewResId() {
        XmlFragment annotation = getClass().getAnnotation(XmlFragment.class);
        if (annotation != null)
            return annotation.layout();
        return 0;
    }

    public String getTitle() {
        XmlFragment annotation = getClass().getAnnotation(XmlFragment.class);
        if (annotation != null)
            return getString(annotation.title());
        return "";
    }

    public void start() {
        onStart();
        running = true;
    }

    public void resume() {
        onResume();
        running = true;
    }

    protected void onResume() {
    }

    public void finish() {
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

    public @NonNull View getView() {
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

    public Animator animateStart() {
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
        if (view instanceof FragmentRootView)
            animator.addListener(new LockListenerAdapter((FragmentRootView) view));
        animator.start();
        return animator;
    }

    public Animator animatePause() {
        ValueAnimator animator = ValueAnimator.ofFloat(1.1f, 1);
        animator.setDuration(200);
        if (view instanceof FragmentRootView)
            animator.addListener(new LockListenerAdapter((FragmentRootView) view));
        animator.start();
        return animator;
    }

    public ValueAnimator animateFinish() {
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
        if (view instanceof FragmentRootView)
            animator.addListener(new LockListenerAdapter((FragmentRootView) view));
        animator.start();
        return animator;
    }

    public Animator animateResume() {
        return null;
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
        bundle.putString(TAG, tag);
    }

    public void onRestoreState(Bundle bundle) {
        view.restoreHierarchyState(bundle.getSparseParcelableArray(HIERARCHY_STATE));
        childFragmentManager.restore(bundle.getBundle(FRAGMENT_MANAGER));

        if (bundle.containsKey(TARGET))
            target = bundle.getInt(TARGET);
        id = bundle.getInt(ID);
        tag = bundle.getString(TAG);
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

    public <T extends Fragment> FragmentTransaction add(T fragment, int id, FragmentTransaction.Mode mode) {
        fragment.setParent(this);
        return childFragmentManager.add(fragment, id, mode);
    }

    public <T extends Fragment> FragmentTransaction add(T fragment, String tag, FragmentTransaction.Mode mode) {
        fragment.setParent(this);
        return childFragmentManager.add(fragment, tag, mode);
    }

    public <T extends Fragment> FragmentTransaction add(Class<T> fragmentClass, int id, FragmentTransaction.Mode mode) {
        T fragment = childFragmentManager.instantiate(fragmentClass);
        fragment.setParent(this);
        return childFragmentManager.add(fragment, id, mode);
    }

    public <T extends Fragment> FragmentTransaction add(Class<T> fragmentClass, String tag, FragmentTransaction.Mode mode) {
        T fragment = childFragmentManager.instantiate(fragmentClass);
        fragment.setParent(this);
        return childFragmentManager.add(fragment, tag, mode);
    }

    public <T extends Fragment> FragmentTransaction replace(T fragment, int id, FragmentTransaction.Mode mode) {
        fragment.setParent(this);
        return childFragmentManager.replace(fragment, id, mode);
    }

    public <T extends Fragment> FragmentTransaction replace(T fragment, String tag, FragmentTransaction.Mode mode) {
        fragment.setParent(this);
        return childFragmentManager.replace(fragment, tag, mode);
    }

    @Override
    public <T extends Fragment, T2 extends Fragment> FragmentTransaction replace(T2 removeFragment, T addFragment, FragmentTransaction.Mode mode) {
        addFragment.setParent(this);
        return childFragmentManager.replace(removeFragment, addFragment, mode);
    }

    public <T extends Fragment> FragmentTransaction replace(Class<T> fragmentClass, int id, FragmentTransaction.Mode mode) {
        T fragment = childFragmentManager.instantiate(fragmentClass);
        fragment.setParent(this);
        return childFragmentManager.replace(fragment, id, mode);
    }

    @Override
    public <T extends Fragment, T2 extends Fragment> FragmentTransaction replace(T2 removeFragment, Class<T> fragmentClass, FragmentTransaction.Mode mode) {
        T fragment = childFragmentManager.instantiate(fragmentClass);
        fragment.setParent(this);
        return childFragmentManager.replace(removeFragment, fragment, mode);
    }

    public <T extends Fragment> FragmentTransaction replace(Class<T> fragmentClass, String tag, FragmentTransaction.Mode mode) {
        T fragment = childFragmentManager.instantiate(fragmentClass);
        fragment.setParent(this);
        return childFragmentManager.replace(fragment, tag, mode);
    }

    public FragmentTransaction remove(int id, FragmentTransaction.Mode mode) {
        return childFragmentManager.remove(id, mode);
    }

    public FragmentTransaction remove(String tag, FragmentTransaction.Mode mode) {
        return childFragmentManager.remove(tag, mode);
    }

    public <T extends Fragment> FragmentTransaction remove(T fragment, FragmentTransaction.Mode mode) {
        return childFragmentManager.remove(fragment, mode);
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

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    protected void onFinish() {
    }

    protected void onStart() {
    }

}
