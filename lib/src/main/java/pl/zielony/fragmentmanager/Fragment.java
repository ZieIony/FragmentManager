package pl.zielony.fragmentmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.nineoldandroids.animation.Animator;
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
    private static final String TARGET = "target";
    private static final String ID = "id";
    private static final String TAG = "tag";
    private static final String FRESH = "fresh";
    private final XmlFragment annotation;

    private Activity activity;
    private View view;
    private Handler handler;
    private FragmentManager fragmentManager;
    private FragmentManager childFragmentManager;
    private Fragment parent;
    private Integer target;
    private int id;
    private static int idSequence = 0;
    private String tag;
    private boolean fresh;
    private boolean started;
    private boolean running;
    private boolean pooling;

    public Fragment() {
        annotation = getClass().getAnnotation(XmlFragment.class);
        clear();
    }

    void clear() {
        fresh = true;
        handler = new Handler();
        childFragmentManager = null;
        id = idSequence++;
    }

    void init(FragmentManager fragmentManager) {
        this.activity = fragmentManager.getActivity();
        this.fragmentManager = fragmentManager;
        childFragmentManager = new FragmentManager(fragmentManager);
        if (view == null)
            view = onCreateView();
        onCreate();
    }

    protected void onCreate() {

    }

    protected View onCreateView() {
        return View.inflate(getContext(), getViewResId(), null);
    }

    protected int getViewResId() {
        if (annotation != null)
            return annotation.layout();
        return 0;
    }

    public String getTitle() {
        if (annotation != null)
            return getString(annotation.title());
        return "";
    }

    public void start() {
        if (started)
            return;
        if (fresh)
            onFreshStart();
        onStart();
        started = true;
        fresh = false;
    }

    protected void onFreshStart() {
    }

    protected void onStart() {
    }

    public void resume() {
        if (running)
            return;
        onResume();
        running = true;
    }

    protected void onResume() {
    }


    public void pause() {
        if (!running)
            return;
        running = false;
        onPause();
    }

    protected void onPause() {
    }

    public void stop() {
        if (!started)
            return;
        started = false;
        onStop();
    }

    protected void onStop() {
    }

    public boolean isRunning() {
        return running;
    }

    public
    @NonNull
    View getView() {
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

    public View findViewWithTag(Object tag) {
        return view.findViewWithTag(tag);
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
        bundle.putBoolean(FRESH, fresh);
    }

    public void onRestoreState(Bundle bundle) {
        view.restoreHierarchyState(bundle.getSparseParcelableArray(HIERARCHY_STATE));
        childFragmentManager.restore(bundle.getBundle(FRAGMENT_MANAGER));

        if (bundle.containsKey(TARGET))
            target = bundle.getInt(TARGET);
        id = bundle.getInt(ID);
        tag = bundle.getString(TAG);
        fresh = bundle.getBoolean(FRESH);
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

    protected void onNewIntent(Intent intent) {

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    public boolean isFresh() {
        return fresh;
    }

    public void setPoolingEnabled(boolean pooling) {
        this.pooling = pooling;
    }

    public boolean isPoolingEnabled() {
        return pooling;
    }
}
