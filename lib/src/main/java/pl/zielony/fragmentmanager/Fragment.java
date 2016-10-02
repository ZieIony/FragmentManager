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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nineoldandroids.animation.Animator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Marcin on 2015-03-20.
 */
public abstract class Fragment {
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

    private static final int NO_TARGET = -1;

    private final FragmentAnnotation annotation;

    private Activity activity;
    private Context context;
    private View view;
    private FragmentRootView rootView;
    private Handler handler;
    private FragmentManager fragmentManager;
    private FragmentManager childFragmentManager;

    private int target = NO_TARGET;
    private static SparseArray<Bundle> results = new SparseArray<>();

    private int id;
    private static int idSequence = 0;
    private String tag;

    private StateMachine stateMachine;
    private int desiredState;

    private boolean pooling;
    private static Map<Class<? extends Fragment>, Fragment> fragmentPool = new HashMap<>();

    private boolean fresh = true;
    private FragmentAnimator fragmentAnimator;

    public Fragment() {
        stateMachine = new StateMachine();
        stateMachine.fragment = this;
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

        stateMachine.addEdge(StateMachine.STATE_NEW, STATE_CREATED, new EdgeListener() {
            @Override
            public boolean canChangeState() {
                return view != null;
            }

            @Override
            public void onStateChanged() {
                onCreate();
            }
        });
        stateMachine.addEdge(STATE_CREATED, STATE_ATTACHED, new EdgeListener() {
            @Override
            public boolean canChangeState() {
                return rootView.isAttached();
            }

            @Override
            public void onStateChanged() {
                onAttach();
            }
        });
        stateMachine.addEdge(STATE_ATTACHED, STATE_STARTED, new EdgeListener() {
            @Override
            public boolean canChangeState() {
                return fragmentManager.isStarted() && (desiredState == STATE_STARTED || desiredState == STATE_RESUMED);
            }

            @Override
            public void onStateChanged() {
                onStart(fresh);
                childFragmentManager.onStart();
                fresh = false;
            }
        });
        stateMachine.addEdge(STATE_STARTED, STATE_RESUMED, new EdgeListener() {
            @Override
            public boolean canChangeState() {
                return fragmentManager.isResumed() && desiredState == STATE_RESUMED;
            }

            @Override
            public void onStateChanged() {
                onResume();
                childFragmentManager.onResume();
            }
        });

        stateMachine.addEdge(STATE_RESUMED, STATE_STARTED, new EdgeListener() {
            @Override
            public boolean canChangeState() {
                return desiredState == STATE_STARTED;
            }

            @Override
            public void onStateChanged() {
                onPause();
                childFragmentManager.onPause();
            }
        });
        stateMachine.addEdge(STATE_STARTED, STATE_ATTACHED, new EdgeListener() {
            @Override
            public boolean canChangeState() {
                return desiredState == STATE_ATTACHED;
            }

            @Override
            public void onStateChanged() {
                onStop();
                childFragmentManager.onStop();
            }
        });
        stateMachine.addEdge(STATE_ATTACHED, STATE_CREATED, new EdgeListener() {
            @Override
            public boolean canChangeState() {
                return !rootView.isAttached();
            }

            @Override
            public void onStateChanged() {
                onDetach();
            }
        });
    }

    void setFragmentManager(FragmentManager manager) {
        this.activity = manager.getActivity();
        this.fragmentManager = manager;
        childFragmentManager = new FragmentManager(activity);
        childFragmentManager.setRoot(rootView);
    }

    private void create(Context context) {
        this.context = context;
        handler = new Handler();
        id = idSequence++;
        if (view == null) {
            rootView = new FragmentRootView(context);
            view = onCreateView();
            rootView.addView(view);
            rootView.addOnLayoutChangeListener(new FragmentRootView.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    stateMachine.update();
                }
            });
            rootView.addOnAttachStateChangeListener(new FragmentRootView.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    stateMachine.update();
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                    stateMachine.update();
                }
            });
        }
        stateMachine.update();
    }

    protected void onCreate() {

    }

    protected void onDestroy() {

    }

    protected View onCreateView() {
        return LayoutInflater.from(getContext()).inflate(getViewResId(), rootView, false);
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

    public void start() {
        desiredState = STATE_STARTED;
        stateMachine.update();
        childFragmentManager.onStart();
    }

    protected void onStart(boolean freshStart) {
    }

    public void resume() {
        desiredState = STATE_RESUMED;
        stateMachine.update();
        childFragmentManager.onResume();
    }

    protected void onResume() {
    }

    public void pause() {
        childFragmentManager.onPause();
        desiredState = STATE_STARTED;
        stateMachine.update();
    }

    protected void onPause() {
    }

    public void stop() {
        childFragmentManager.onStop();
        desiredState = STATE_ATTACHED;
        stateMachine.update();
    }

    protected void onStop() {
    }

    public boolean isAttached() {
        return stateMachine.getState() == STATE_ATTACHED || stateMachine.getState() == STATE_STARTED || stateMachine.getState() == STATE_RESUMED;
    }

    public void detach() {
        childFragmentManager.onDetach();
        view.setAnimation(null);
        ((ViewGroup) rootView.getParent()).removeView(rootView);
    }

    public boolean isCreated() {
        return stateMachine.getState() != StateMachine.STATE_NEW;
    }

    public void destroy(){
        handler = null;
        childFragmentManager = null;
        id = -1;
        activity = null;
        fragmentManager = null;
        fresh = true;
        stateMachine.resetState();
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
        return context;
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

        if (target != NO_TARGET)
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
        if (target == NO_TARGET)
            return;
        results.append(target, result);
    }

    public String getString(int resId) {
        return activity.getString(resId);
    }

    public String getString(int resId, Object... args) {
        return activity.getString(resId, args);
    }

    public <T extends Fragment> void add(T fragment, int id, TransactionMode mode) {
        childFragmentManager.add(fragment, id, mode);
    }

    public <T extends Fragment> void add(T fragment, String tag, TransactionMode mode) {
        childFragmentManager.add(fragment, tag, mode);
    }

    public <T extends Fragment> void add(Class<T> fragmentClass, int id, TransactionMode mode) {
        T fragment = Fragment.instantiate(fragmentClass, activity);
        childFragmentManager.add(fragment, id, mode);
    }

    public <T extends Fragment> void add(Class<T> fragmentClass, String tag, TransactionMode mode) {
        T fragment = Fragment.instantiate(fragmentClass, activity);
        childFragmentManager.add(fragment, tag, mode);
    }

    public <T extends Fragment> void replace(T fragment, int id, TransactionMode mode) {
        childFragmentManager.replace(fragment, id, mode);
    }

    public <T extends Fragment> void replace(T fragment, String tag, TransactionMode mode) {
        childFragmentManager.replace(fragment, tag, mode);
    }

    public <T extends Fragment, T2 extends Fragment> void replace(T2 removeFragment, T addFragment, TransactionMode mode) {
        childFragmentManager.replace(removeFragment, addFragment, mode);
    }

    public <T extends Fragment> void replace(Class<T> fragmentClass, int id, TransactionMode mode) {
        T fragment = Fragment.instantiate(fragmentClass, activity);
        childFragmentManager.replace(fragment, id, mode);
    }

    public <T extends Fragment, T2 extends Fragment> void replace(T2 removeFragment, Class<T> fragmentClass, TransactionMode mode) {
        T fragment = Fragment.instantiate(fragmentClass, activity);
        childFragmentManager.replace(removeFragment, fragment, mode);
    }

    public <T extends Fragment> void replace(Class<T> fragmentClass, String tag, TransactionMode mode) {
        T fragment = Fragment.instantiate(fragmentClass, activity);
        childFragmentManager.replace(fragment, tag, mode);
    }

    public void remove(int id, TransactionMode mode) {
        childFragmentManager.remove(id, mode);
    }

    public void remove(String tag, TransactionMode mode) {
        childFragmentManager.remove(tag, mode);
    }

    public <T extends Fragment> void remove(T fragment, TransactionMode mode) {
        childFragmentManager.remove(fragment, mode);
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

    public static <T extends Fragment> T instantiate(Class<T> fragmentClass, Context context) {
        Fragment fromPool = fragmentPool.remove(fragmentClass);
        if (fromPool != null) {
            fromPool.create(context);
            return (T) fromPool;
        }

        Fragment fragment;
        try {
            fragment = fragmentClass.getConstructor().newInstance();
            fragment.create(context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return (T) fragment;
    }

    public static void pool(Class<? extends Fragment> fragmentClass, Context context) {
        FragmentAnnotation annotation = fragmentClass.getAnnotation(FragmentAnnotation.class);
        if (annotation != null && !annotation.pooling())
            throw new RuntimeException(fragmentClass.getSimpleName() + " cannot be pooled because pooling is disabled by annotation");
        if (!fragmentPool.containsKey(fragmentClass))
            fragmentPool.put(fragmentClass, instantiate(fragmentClass, context));
    }

    public static void pool(Fragment fragment) {
        if (!fragment.isPoolingEnabled())
            return;
        if (!fragmentPool.containsKey(fragment.getClass()))
            fragmentPool.put(fragment.getClass(), fragment);
    }

    // wyczyść basen :D
    public static void clearPool() {
        fragmentPool.clear();
    }

    public int getTarget() {
        return target;
    }

    /**
     * Gets and removes the result
     *
     * @param id
     * @return
     */
    public static Bundle getResult(int id) {
        results.remove(id);
        return results.get(id);
    }

    public boolean onNavigate(Fragment fragment, TransactionMode mode) {
        return false;
    }
}
