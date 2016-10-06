package pl.zielony.fragmentmanager;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.zielony.animator.Animator;

/**
 * Created by Marcin on 2015-03-20.
 */
public abstract class Fragment extends FragmentManager {

    private View view;
    private FragmentRootView rootView;

    private boolean pooling;
    private static Map<Class<? extends Fragment>, Fragment> fragmentPool = new HashMap<>();

    private FragmentAnimator fragmentAnimator;

    private FragmentAnnotation annotation;

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

    @NonNull
    public FragmentRootView getRootView() {
        return rootView;
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

    protected void create(Context context) {
        super.create(context);
        if (view == null) {
            rootView = new FragmentRootView(context);
            view = onCreateView();
            view.setVisibility(View.INVISIBLE);
            rootView.addView(view);
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
        getStateMachine().update();
    }

    @Override
    public void detach() {
        view.setAnimation(null);
        ((ViewGroup) rootView.getParent()).removeView(rootView);
    }

    public void save(Bundle bundle) {
        SparseArray<Parcelable> container = new SparseArray<>();
        view.saveHierarchyState(container);
        bundle.putSparseParcelableArray(HIERARCHY_STATE, container);
        super.save(bundle);
    }

    public void restore(Bundle bundle) {
        super.restore(bundle);
        view.restoreHierarchyState(bundle.getSparseParcelableArray(HIERARCHY_STATE));
    }

    @Override
    protected void initStateMachineStates() {
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
                return desiredState == STATE_STARTED || desiredState == STATE_RESUMED;
            }

            @Override
            public void onStateChanged() {
                onStart(fresh);
                fresh = false;
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
                return !rootView.isAttached();
            }

            @Override
            public void onStateChanged() {
                onDetach();
            }
        });
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


}
