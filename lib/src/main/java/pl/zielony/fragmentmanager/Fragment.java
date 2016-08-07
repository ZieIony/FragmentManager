package pl.zielony.fragmentmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import com.nineoldandroids.animation.Animator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marcin on 2015-03-20.
 */
public abstract class Fragment {
    public static final int ADD = 1;
    public static final int ACTIVITY = 2;
    public static final int REMOVE = 4;

    private static final int STATE_CREATED = 1;
    private static final int STATE_ATTACHED = 2;
    private static final int STATE_STARTED = 3;
    private static final int STATE_RESUMED = 4;

    private static final String FRAGMENT_MANAGER = "fragmentManager";
    private static final String HIERARCHY_STATE = "hierarchyState";
    private static final String TARGET = "target";
    private static final String ID = "id";
    private static final String TAG = "tag";
    private static final String FRESH = "fresh";
    private final FragmentAnnotation annotation;

    private Activity activity;
    private View view;
    private FragmentRootView rootView;
    private Handler handler;
    private FragmentManager fragmentManager;
    private FragmentManager childFragmentManager;
    private Fragment parent;
    private Integer target;
    private int id;
    private static int idSequence = 0;
    private String tag;

    private StateMachine stateMachine = new StateMachine();

    private boolean pooling;
    private boolean fresh = true;
    private FragmentAnimator fragmentAnimator;

    public Fragment() {
        annotation = getClass().getAnnotation(FragmentAnnotation.class);
        if (annotation != null) {
            pooling = annotation.pooling();
            Class animatorClass = annotation.animator();
            if (animatorClass != Void.class) {
                try {
                    fragmentAnimator = (FragmentAnimator) animatorClass.getConstructor().newInstance();
                } catch (Exception e) {
                }
            }
        }

        stateMachine.addEdge(StateMachine.STATE_NEW, STATE_CREATED, new EdgeListener<Void>() {
            @Override
            public void onEdge(Void param) {
                onCreate();
            }
        });
        stateMachine.addEdge(STATE_CREATED, STATE_ATTACHED, new EdgeListener<Void>() {
            @Override
            public void onEdge(Void param) {
                onAttach();
            }
        });
        stateMachine.addEdge(STATE_ATTACHED, STATE_STARTED, new EdgeListener<Integer>() {
            @Override
            public void onEdge(Integer param) {
                onStart(param);
                fresh = false;
            }
        });
        stateMachine.addEdge(STATE_STARTED, STATE_RESUMED, new EdgeListener<Integer>() {
            @Override
            public void onEdge(Integer param) {
                onResume(param);
            }
        });

        stateMachine.addEdge(STATE_RESUMED, STATE_STARTED, new EdgeListener<Integer>() {
            @Override
            public void onEdge(Integer param) {
                onPause(param);
            }
        });
        stateMachine.addEdge(STATE_STARTED, STATE_ATTACHED, new EdgeListener<Integer>() {
            @Override
            public void onEdge(Integer param) {
                onStop(param);
            }
        });
        stateMachine.addEdge(STATE_ATTACHED, STATE_CREATED, new EdgeListener<Void>() {
            @Override
            public void onEdge(Void param) {
                onDetach();
            }
        });
    }

    void clear() {
        handler = null;
        childFragmentManager = null;
        id = -1;
        activity = null;
        fragmentManager = null;
        fresh = true;
        stateMachine.resetState();
    }

    void init(FragmentManager fragmentManager) {
        handler = new Handler();
        id = idSequence++;
        this.activity = fragmentManager.getActivity();
        this.fragmentManager = fragmentManager;
        childFragmentManager = new FragmentManager(fragmentManager);
        if (view == null) {
            rootView = new FragmentRootView(activity);
            view = onCreateView();
            rootView.addView(view);
            rootView.addOnLayoutChangeListener(new FragmentRootView.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (stateMachine.getState() == STATE_CREATED)
                        stateMachine.setState(STATE_ATTACHED);
                }
            });
            rootView.addOnAttachStateChangeListener(new FragmentRootView.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    if (rootView.getWidth() > 0 && rootView.getHeight() > 0 && stateMachine.getState() == STATE_CREATED)
                        stateMachine.setState(STATE_ATTACHED);
                }

                @Override
                public void onViewDetachedFromWindow(View v) {

                }
            });
        }
        childFragmentManager.setRoot(rootView);
        stateMachine.setState(STATE_CREATED);
    }

    protected void onCreate() {

    }

    protected void onDestroy() {

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

    protected void onAttach() {

    }

    protected void onDetach() {

    }

    public void start(int detail) {
        stateMachine.queueState(STATE_STARTED, detail | (fresh ? ADD : 0));
    }

    protected void onStart(int detail) {
    }

    public void resume(int detail) {
        stateMachine.queueState(STATE_RESUMED, detail);
    }

    protected void onResume(int detail) {
    }

    public void pause(int detail) {
        stateMachine.queueState(STATE_STARTED, detail);
    }

    protected void onPause(int detail) {
    }

    public void stop(int detail) {
        stateMachine.queueState(STATE_ATTACHED, detail);
    }

    protected void onStop(int detail) {
    }

    public boolean isAttached() {
        return stateMachine.getState() == STATE_ATTACHED || stateMachine.getState() == STATE_STARTED || stateMachine.getState() == STATE_RESUMED;
    }

    public boolean isStarted() {
        return stateMachine.getState() == STATE_STARTED || stateMachine.getState() == STATE_RESUMED;
    }

    public boolean isResumed() {
        return stateMachine.getState() == STATE_RESUMED;
    }

    @NonNull
    public FragmentRootView getRootView() {
        return rootView;
    }

    @NonNull
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
        groups.add(rootView);

        while (!groups.isEmpty()) {
            ViewGroup group = groups.remove(0);

            for (int i = 0; i < group.getChildCount(); ++i) {
                View child = group.getChildAt(i);
                if (child.getId() == id)
                    result.add(child);

                if (child instanceof ViewGroup)
                    groups.add((ViewGroup) child);
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
        groups.add(rootView);

        while (!groups.isEmpty()) {
            ViewGroup group = groups.remove(0);

            for (int i = 0; i < group.getChildCount(); ++i) {
                View child = group.getChildAt(i);
                if (tag.equals(child.getTag()))
                    result.add(child);

                if (child instanceof ViewGroup)
                    groups.add((ViewGroup) child);
            }
        }

        return result;
    }

    public Animator animateAdd() {
        if (fragmentAnimator == null)
            return null;
        return fragmentAnimator.animateAdd(this);
    }

    public Animator animateStop() {
        if (fragmentAnimator == null)
            return null;
        return fragmentAnimator.animateStop(this);
    }

    public Animator animateRemove() {
        if (fragmentAnimator == null)
            return null;
        return fragmentAnimator.animateRemove(this);
    }

    public Animator animateStart() {
        if (fragmentAnimator == null)
            return null;
        return fragmentAnimator.animateStart(this);
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

    public <T extends Fragment> FragmentTransaction add(T fragment, int id, TransactionMode mode) {
        fragment.setParent(this);
        return childFragmentManager.add(fragment, id, mode);
    }

    public <T extends Fragment> FragmentTransaction add(T fragment, String tag, TransactionMode mode) {
        fragment.setParent(this);
        return childFragmentManager.add(fragment, tag, mode);
    }

    public <T extends Fragment> FragmentTransaction add(Class<T> fragmentClass, int id, TransactionMode mode) {
        T fragment = childFragmentManager.instantiate(fragmentClass);
        fragment.setParent(this);
        return childFragmentManager.add(fragment, id, mode);
    }

    public <T extends Fragment> FragmentTransaction add(Class<T> fragmentClass, String tag, TransactionMode mode) {
        T fragment = childFragmentManager.instantiate(fragmentClass);
        fragment.setParent(this);
        return childFragmentManager.add(fragment, tag, mode);
    }

    public <T extends Fragment> FragmentTransaction replace(T fragment, int id, TransactionMode mode) {
        fragment.setParent(this);
        return childFragmentManager.replace(fragment, id, mode);
    }

    public <T extends Fragment> FragmentTransaction replace(T fragment, String tag, TransactionMode mode) {
        fragment.setParent(this);
        return childFragmentManager.replace(fragment, tag, mode);
    }

    public <T extends Fragment, T2 extends Fragment> FragmentTransaction replace(T2 removeFragment, T addFragment, TransactionMode mode) {
        addFragment.setParent(this);
        return childFragmentManager.replace(removeFragment, addFragment, mode);
    }

    public <T extends Fragment> FragmentTransaction replace(Class<T> fragmentClass, int id, TransactionMode mode) {
        T fragment = childFragmentManager.instantiate(fragmentClass);
        fragment.setParent(this);
        return childFragmentManager.replace(fragment, id, mode);
    }

    public <T extends Fragment, T2 extends Fragment> FragmentTransaction replace(T2 removeFragment, Class<T> fragmentClass, TransactionMode mode) {
        T fragment = childFragmentManager.instantiate(fragmentClass);
        fragment.setParent(this);
        return childFragmentManager.replace(removeFragment, fragment, mode);
    }

    public <T extends Fragment> FragmentTransaction replace(Class<T> fragmentClass, String tag, TransactionMode mode) {
        T fragment = childFragmentManager.instantiate(fragmentClass);
        fragment.setParent(this);
        return childFragmentManager.replace(fragment, tag, mode);
    }

    public FragmentTransaction remove(int id, TransactionMode mode) {
        return childFragmentManager.remove(id, mode);
    }

    public FragmentTransaction remove(String tag, TransactionMode mode) {
        return childFragmentManager.remove(tag, mode);
    }

    public <T extends Fragment> FragmentTransaction remove(T fragment, TransactionMode mode) {
        return childFragmentManager.remove(fragment, mode);
    }

    public boolean back() {
        return childFragmentManager.back();
    }

    public boolean up() {
        return childFragmentManager.up();
    }

    public boolean hasBack() {
        return childFragmentManager.hasBack();
    }

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

    public void setPoolingEnabled(boolean pooling) {
        this.pooling = pooling;
    }

    public boolean isPoolingEnabled() {
        return pooling;
    }

    protected boolean onKeyEvent(KeyEvent event) {
        return false;
    }

    public FragmentAnimator getAnimator() {
        return fragmentAnimator;
    }

    public void setAnimator(FragmentAnimator fragmentAnimator) {
        this.fragmentAnimator = fragmentAnimator;
    }

    public void setOnStateChangeListener(OnStateChangeListener stateListener) {
        stateMachine.setOnStateChangeListener(stateListener);
    }
}
