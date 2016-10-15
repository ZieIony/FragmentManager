package pl.zielony.fragmentmanager;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import pl.zielony.animator.Animator;

/**
 * Created by Marcin on 2015-03-20.
 */
public abstract class Fragment extends ManagerBase {
    public static final String HIERARCHY_STATE = "hierarchyState";

    private static final String TARGET = "target";
    private static final String ID = "id";
    private static final String TAG = "tag";

    private static final int NO_TARGET = -1;

    private View view;

    private boolean pooling;

    private FragmentAnimator fragmentAnimator;
    private ManagerBase manager;

    private FragmentAnnotation annotation;

    private int target = NO_TARGET;
    private static SparseArray<Bundle> results = new SparseArray<>();

    private int id;
    private static int idSequence = 0;
    private String tag;

    public Fragment() {
        annotation = getClass().getAnnotation(FragmentAnnotation.class);
        if (annotation != null) {
            pooling = annotation.pooling();
            Class<? extends FragmentAnimator> animatorClass = annotation.animator();
            if (animatorClass != FragmentAnimator.EMPTY) {
                try {
                    fragmentAnimator = animatorClass.getConstructor().newInstance();
                } catch (Exception e) {
                }
            }
        }
        initStateMachineStates();
    }

    private void initStateMachineStates() {
        StateMachine stateMachine = getStateMachine();
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
                return getRootView().isAttached();
            }

            @Override
            public void onStateChanged() {
                onAttach();
            }
        });
        stateMachine.addEdge(STATE_ATTACHED, STATE_STARTED, new EdgeListener() {
            @Override
            public boolean canChangeState() {
                return desiredState == STATE_STARTED || desiredState == STATE_RESUMED;
            }

            @Override
            public void onStateChanged() {
                onStart();
            }
        });
        stateMachine.addEdge(STATE_STARTED, STATE_RESUMED, new EdgeListener() {
            @Override
            public boolean canChangeState() {
                return desiredState == STATE_RESUMED;
            }

            @Override
            public void onStateChanged() {
                onResume();
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
            }
        });
        stateMachine.addEdge(STATE_ATTACHED, STATE_CREATED, new EdgeListener() {
            @Override
            public boolean canChangeState() {
                return !getRootView().isAttached();
            }

            @Override
            public void onStateChanged() {
                onDetach();
            }
        });
    }

    protected View onCreateView() {
        return LayoutInflater.from(getActivity()).inflate(getViewResId(), getRootView(), false);
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

    @NonNull
    public View getView() {
        return view;
    }

    public View findViewById(int id) {
        return view.findViewById(id);
    }

    public List<View> findViewsById(int id) {
        ArrayList<View> result = new ArrayList<>();
        ArrayList<ViewGroup> groups = new ArrayList<>();
        groups.add(getRootView());

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
        groups.add(getRootView());

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
        return fragmentAnimator == null ? null : fragmentAnimator.animateAdd(this);
    }

    public Animator animateStop() {
        return fragmentAnimator == null ? null : fragmentAnimator.animateStop(this);
    }

    public Animator animateRemove() {
        return fragmentAnimator == null ? null : fragmentAnimator.animateRemove(this);
    }

    public Animator animateStart() {
        return fragmentAnimator == null ? null : fragmentAnimator.animateStart(this);
    }

    protected void create(Activity activity, Bundle state) {
        this.activity = activity;
        id = idSequence++;
        if (rootView == null) {
            rootView = new FragmentRootView(activity);
            rootView.addOnLayoutChangeListener(new FragmentRootView.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    getStateMachine().update();
                }
            });
            rootView.addOnAttachStateChangeListener(new FragmentRootView.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    getStateMachine().update();
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                    getStateMachine().update();
                }
            });
        }
        if (view == null) {
            view = onCreateView();
            view.setVisibility(View.INVISIBLE);
            getRootView().addView(view);
        }

        this.userState = state;

        getStateMachine().update();
    }

    public void destroy() {
        if (!isCreated())
            return;
        id = -1;
        manager = null;
        super.destroy();
    }

    public void save(Bundle state) {
        super.save(state);

        if (target != NO_TARGET)
            state.putInt(TARGET, target);
        state.putInt(ID, id);
        state.putString(TAG, tag);

        SparseArray<Parcelable> container = new SparseArray<>();
        view.saveHierarchyState(container);
        state.putSparseParcelableArray(HIERARCHY_STATE, container);
    }

    public void restore(Bundle state) {
        super.restore(state);

        if (state.containsKey(TARGET))
            target = state.getInt(TARGET);
        id = state.getInt(ID);
        tag = state.getString(TAG);

        view.restoreHierarchyState(state.getSparseParcelableArray(HIERARCHY_STATE));
    }

    public void setPoolingEnabled(boolean pooling) {
        this.pooling = pooling;
    }

    public boolean isPoolingEnabled() {
        return pooling;
    }

    public FragmentAnimator getAnimator() {
        return fragmentAnimator;
    }

    public void setAnimator(FragmentAnimator fragmentAnimator) {
        this.fragmentAnimator = fragmentAnimator;
    }

    public static <T extends Fragment> T instantiate(Class<T> fragmentClass, Activity activity) {
        return instantiate(fragmentClass, activity, null);
    }

    public static <T extends Fragment> T instantiate(Class<T> fragmentClass, Activity activity, Bundle state) {
        Fragment fragment = FragmentPool.remove(fragmentClass);
        if (fragment == null) {
            try {
                fragment = fragmentClass.getConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        fragment.create(activity, state);

        if (state != null)
            fragment.restore(state);

        return (T) fragment;
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

    public int getId() {
        return id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
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

    void setManager(ManagerBase manager) {
        //this.activity = manager.getActivity();
        this.manager = manager;
    }

    public ManagerBase getManager() {
        return manager;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + hashCode() % 100 + " [id:" + id + ", tag:" + tag + "]";
    }
}
