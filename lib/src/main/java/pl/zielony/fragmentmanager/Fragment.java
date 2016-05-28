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
    static final String TAG = "tag";

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
    private String tag;

    class LockListenerAdapter implements Animator.AnimatorListener {
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
        return 0;
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
        if (view instanceof FragmentRootView)
            animator.addListener(new LockListenerAdapter((FragmentRootView) view));
        animator.start();
    }

    public void animateOutAdd(AnimatorListenerAdapter listener) {
        ValueAnimator animator = ValueAnimator.ofFloat(1.1f, 1);
        animator.setDuration(200);
        if (listener != null)
            animator.addListener(listener);
        if (view instanceof FragmentRootView)
            animator.addListener(new LockListenerAdapter((FragmentRootView) view));
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
        if (view instanceof FragmentRootView)
            animator.addListener(new LockListenerAdapter((FragmentRootView) view));
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

    public <T extends Fragment> T add(T fragment, int id, FragmentState.Mode mode) {
        T f = childFragmentManager.add(fragment, id, mode);
        f.setParent(this);
        return f;
    }

    public <T extends Fragment> T add(T fragment, String tag, FragmentState.Mode mode) {
        T f = childFragmentManager.add(fragment, tag, mode);
        f.setParent(this);
        return f;
    }

    public <T extends Fragment> T add(Class<T> fragmentClass, int id, FragmentState.Mode mode) {
        T f = childFragmentManager.add(fragmentClass, id, mode);
        f.setParent(this);
        return f;
    }

    public <T extends Fragment> T add(Class<T> fragmentClass, String tag, FragmentState.Mode mode) {
        T f = childFragmentManager.add(fragmentClass, tag, mode);
        f.setParent(this);
        return f;
    }

    public <T extends Fragment> T replace(T fragment, int id, FragmentState.Mode mode) {
        T f = childFragmentManager.replace(fragment, id, mode);
        f.setParent(this);
        return f;
    }

    public <T extends Fragment> T replace(T fragment, String tag, FragmentState.Mode mode) {
        T f = childFragmentManager.replace(fragment, tag, mode);
        f.setParent(this);
        return f;
    }

    @Override
    public <T extends Fragment, T2 extends Fragment> T replace(T2 removeFragment, T addFragment, FragmentState.Mode mode) {
        T f = childFragmentManager.replace(removeFragment, addFragment, mode);
        f.setParent(this);
        return f;
    }

    public <T extends Fragment> T replace(Class<T> fragmentClass, int id, FragmentState.Mode mode) {
        T f = childFragmentManager.replace(fragmentClass, id, mode);
        f.setParent(this);
        return f;
    }

    @Override
    public <T extends Fragment, T2 extends Fragment> T replace(T2 removeFragment, Class<T> fragmentClass, FragmentState.Mode mode) {
        T f = childFragmentManager.replace(removeFragment, fragmentClass, mode);
        f.setParent(this);
        return f;
    }

    public <T extends Fragment> T replace(Class<T> fragmentClass, String tag, FragmentState.Mode mode) {
        T f = childFragmentManager.replace(fragmentClass, tag, mode);
        f.setParent(this);
        return f;
    }

    public void remove(int id, FragmentState.Mode mode) {
        childFragmentManager.remove(id, mode);
    }

    public void remove(String tag, FragmentState.Mode mode) {
        childFragmentManager.remove(tag, mode);
    }

    public <T extends Fragment> void remove(T fragment, FragmentState.Mode mode) {
        childFragmentManager.remove(fragment, mode);
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
