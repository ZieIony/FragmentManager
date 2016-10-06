package pl.zielony.fragmentmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import pl.zielony.animator.Animator;
import pl.zielony.animator.AnimatorListenerAdapter;

/**
 * Created by Marcin on 2016-10-03.
 */

public class FragmentManager {
    private static final String ACTIVE_STATES = FragmentManager.class.getName() + "fragmentManagerActiveStates";
    private static final String STATES = FragmentManager.class.getName() + "fragmentManagerStates";
    private static final String TRANSACTIONS = FragmentManager.class.getName() + "fragmentManagerTransactions";

    public static final int STATE_CREATED = 1;
    public static final int STATE_ATTACHED = 2;
    public static final int STATE_STARTED = 3;
    public static final int STATE_RESUMED = 4;

    public static final String HIERARCHY_STATE = "hierarchyState";
    private static final String TARGET = "target";
    private static final String ID = "id";
    private static final String TAG = "tag";
    private static final String FRESH = "fresh";

    private static final int NO_TARGET = -1;

    List<FragmentTransaction> backstack = new ArrayList<>();
    List<FragmentState> activeStates = new ArrayList<>();
    private FragmentRootView root;

    private Activity activity;
    private Context context;
    static Handler handler = new Handler(Looper.getMainLooper());
    private FragmentManager fragmentManager;

    private int target = NO_TARGET;
    private static SparseArray<Bundle> results = new SparseArray<>();

    private int id;
    private static int idSequence = 0;
    private String tag;

    private StateMachine stateMachine;
    protected int desiredState;

    protected boolean fresh = true;

    public FragmentManager() {
        init();
    }

    private void init() {
        stateMachine = new StateMachine();
        stateMachine.fragment = this;
        initStateMachineStates();
    }

    protected void initStateMachineStates() {
        StateMachine stateMachine = getStateMachine();
        stateMachine.addEdge(StateMachine.STATE_NEW, STATE_CREATED, new EdgeListener() {
            @Override
            public boolean canChangeState() {
                return desiredState >= STATE_CREATED;
            }

            @Override
            public void onStateChanged() {
                onCreate();
            }
        });
        stateMachine.addEdge(STATE_CREATED, STATE_ATTACHED, new EdgeListener() {
            @Override
            public boolean canChangeState() {
                return desiredState >= STATE_ATTACHED;
            }

            @Override
            public void onStateChanged() {
                onAttach();
            }
        });
        stateMachine.addEdge(STATE_ATTACHED, STATE_STARTED, new EdgeListener() {
            @Override
            public boolean canChangeState() {
                return desiredState >= STATE_STARTED;
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
                return desiredState <= STATE_STARTED;
            }

            @Override
            public void onStateChanged() {
                onPause();
            }
        });
        stateMachine.addEdge(STATE_STARTED, STATE_ATTACHED, new EdgeListener() {
            @Override
            public boolean canChangeState() {
                return desiredState <= STATE_ATTACHED;
            }

            @Override
            public void onStateChanged() {
                onStop();
            }
        });
        stateMachine.addEdge(STATE_ATTACHED, STATE_CREATED, new EdgeListener() {
            @Override
            public boolean canChangeState() {
                return desiredState == STATE_CREATED;
            }

            @Override
            public void onStateChanged() {
                onDetach();
            }
        });
    }

    public FragmentManager(Activity activity) {
        this.activity = activity;
        init();
    }


    // -------------------
    // add
    // -------------------

    public <T extends Fragment> void add(T fragment, int id, TransactionMode mode) {
        FragmentTransaction transaction = new FragmentTransaction(this, mode);
        transaction.add(fragment, id);
        transaction.execute();
    }

    public <T extends Fragment> void add(T fragment, String tag, TransactionMode mode) {
        FragmentTransaction transaction = new FragmentTransaction(this, mode);
        transaction.add(fragment, tag);
        transaction.execute();
    }

    public <T extends Fragment> void add(Class<T> fragmentClass, int id, TransactionMode mode) {
        FragmentTransaction transaction = new FragmentTransaction(this, mode);
        transaction.add(fragmentClass, id);
        transaction.execute();
    }

    public <T extends Fragment> void add(Class<T> fragmentClass, String tag, TransactionMode mode) {
        FragmentTransaction transaction = new FragmentTransaction(this, mode);
        transaction.add(fragmentClass, tag);
        transaction.execute();
    }


    // -------------------
    // replace
    // -------------------

    public <T extends Fragment> void replace(T fragment, int id, TransactionMode mode) {
        FragmentTransaction transaction = new FragmentTransaction(this, mode);
        transaction.replace(fragment, id);
        transaction.execute();
    }

    public <T extends Fragment> void replace(T fragment, String tag, TransactionMode mode) {
        FragmentTransaction transaction = new FragmentTransaction(this, mode);
        transaction.replace(fragment, tag);
        transaction.execute();
    }

    public <T extends Fragment, T2 extends Fragment> void replace(T2 removeFragment, T fragment, TransactionMode mode) {
        FragmentTransaction transaction = new FragmentTransaction(this, mode);
        transaction.replace(removeFragment, fragment);
        transaction.execute();
    }

    public <T extends Fragment> void replace(Class<T> fragmentClass, int id, TransactionMode mode) {
        FragmentTransaction transaction = new FragmentTransaction(this, mode);
        transaction.replace(fragmentClass, id);
        transaction.execute();
    }

    public <T extends Fragment> void replace(Class<T> fragmentClass, String tag, TransactionMode mode) {
        FragmentTransaction transaction = new FragmentTransaction(this, mode);
        transaction.replace(fragmentClass, tag);
        transaction.execute();
    }

    public <T extends Fragment, T2 extends Fragment> void replace(T2 removeFragment, Class<T> fragmentClass, TransactionMode mode) {
        FragmentTransaction transaction = new FragmentTransaction(this, mode);
        transaction.replace(removeFragment, fragmentClass);
        transaction.execute();
    }


    // -------------------
    // remove
    // -------------------

    public void remove(int id, TransactionMode mode) {
        FragmentTransaction transaction = new FragmentTransaction(this, mode);
        transaction.remove(id);
        transaction.execute();
    }

    public void remove(String tag, TransactionMode mode) {
        FragmentTransaction transaction = new FragmentTransaction(this, mode);
        transaction.remove(tag);
        transaction.execute();
    }

    public <T extends Fragment> void remove(T fragment, TransactionMode mode) {
        FragmentTransaction transaction = new FragmentTransaction(this, mode);
        transaction.remove(fragment);
        transaction.execute();
    }

    public boolean upTraverse() {
        for (int i = activeStates.size() - 1; i >= 0; i--) {
            if (activeStates.get(i).getFragment().up())
                return true;
        }
        if (!hasUp())
            return false;
        while (backstack.size() != 0) {
            FragmentTransaction transaction = backstack.remove(backstack.size() - 1);
            TransactionMode mode = transaction.getMode();
            transaction.undo();
            if (mode == TransactionMode.Push)
                return true;
        }
        return false;
    }

    public boolean up() {
        for (int i = activeStates.size() - 1; i >= 0; i--) {
            if (activeStates.get(i).getFragment().upTraverse())
                return true;
        }
        if (!hasUp())
            return false;
        while (backstack.size() != 0) {
            FragmentTransaction transaction = backstack.remove(backstack.size() - 1);
            TransactionMode mode = transaction.getMode();
            transaction.undo();
            if (mode == TransactionMode.Push)
                return true;
        }
        return false;
    }

    /**
     * Pops one backstack step. Traverses through all child fragment managers
     *
     * @return if back could pop one complete step
     */
    public boolean backTraverse() {
        for (int i = activeStates.size() - 1; i >= 0; i--) {
            if (activeStates.get(i).getFragment().backTraverse())
                return true;
        }
        return back();
    }


    /**
     * Pops one backstack step
     *
     * @return if back could pop one complete step
     */
    public boolean back() {
        if (!hasBack())
            return false;
        while (backstack.size() != 0) {
            FragmentTransaction transaction = backstack.remove(backstack.size() - 1);
            TransactionMode mode = transaction.getMode();
            transaction.undo();
            if (mode != TransactionMode.Join)
                return true;
        }
        return false;
    }

    void startState(final FragmentState state) {
        if (state.getFragment() == null)
            state.instantiateFragment(activity);
        Fragment fragment = state.getFragment();
        Log.e("start state", "[" + fragment.getClass().getSimpleName() + ":" + fragment.hashCode() % 100 + "]");
        FragmentRootView rootView = fragment.getRootView();
        if (rootView.getParent() != null || fragment.getFragmentManager() != null)
            throw new IllegalStateException("fragment is already in use");
        final View view = fragment.getView();
        view.setVisibility(View.INVISIBLE);
        fragment.setFragmentManager(this);
        synchronized (FragmentManager.class) {
            activeStates.add(state);
        }
        ViewGroup container = getContainer(state, root);
        container.addView(rootView);
        Bundle fragmentState = state.getState();
        if (!fragmentState.isEmpty()) {
            fragment.restore(fragmentState);
            fragmentState.clear();
        }
        Bundle result = Fragment.getResult(fragment.getId());
        if (result != null)
            fragment.onResult(result);
        synchronized (FragmentManager.class) {
            fragment.start();
        }
    }

    Animator prepareAddAnimation(final FragmentState state, final Animator animator) {
        Fragment fragment = state.getFragment();
        final View view = fragment.getView();
        if (!isResumed()) {
            view.setVisibility(View.VISIBLE);
            resumeState(state);
            return null;
        }
        if (animator != null) {
            FragmentRootView rootView = fragment.getRootView();
            animator.addListener(new LockListenerAdapter(rootView));
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onStart() {
                    view.setVisibility(View.VISIBLE);
                }

                @Override
                public void onEnd() {
                    resumeState(state);
                }
            });
        } else {
            view.setVisibility(View.VISIBLE);
            resumeState(state);
        }
        return animator;
    }

    private void resumeState(FragmentState state) {
        synchronized (FragmentManager.class) {
            state.getFragment().resume();
        }
    }

    Animator prepareRemoveAnimation(final FragmentState state, final Animator animator) {
        final Fragment fragment = state.getFragment();
        final View view = fragment.getView();
        if (!isResumed()) {
            view.setVisibility(View.INVISIBLE);
            removeState(state);
            return null;
        }
        if (animator != null) {
            final FragmentRootView rootView = fragment.getRootView();
            animator.addListener(new LockListenerAdapter(rootView));
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onEnd() {
                    view.setVisibility(View.INVISIBLE);
                    removeState(state);
                    animator.removeListener(this);
                }
            });
        } else {
            view.setVisibility(View.INVISIBLE);
            removeState(state);
        }
        return animator;
    }

    private void removeState(FragmentState state) {
        final Fragment fragment = state.getFragment();
        final View rootView = fragment.getRootView();
        final ViewGroup container = (ViewGroup) rootView.getParent();
        Log.e("stop state", "[" + fragment.getClass().getSimpleName() + ":" + fragment.hashCode() % 100 + "]");
        if (container == null || !activeStates.contains(state))
            throw new IllegalStateException("fragment's container has to be in use!");
        fragment.save(state.getState());
        if (fragment.isResumed())
            fragment.pause();
        if (fragment.isStarted())
            fragment.stop();
        if (fragment.isAttached())
            fragment.detach();
        if (fragment.isCreated())
            fragment.destroy();
        synchronized (FragmentManager.class) {
            activeStates.remove(state);
        }
        state.clearFragment();
        Fragment.pool(fragment);
    }

    public boolean hasBack() {
        for (FragmentState state : activeStates)
            if (state.getFragment().hasBack())
                return true;
        for (FragmentTransaction transaction : backstack)
            if (transaction.getMode() != TransactionMode.Join)
                return true;

        return false;
    }

    public boolean hasUp() {
        for (FragmentState state : activeStates)
            if (state.getFragment().hasUp())
                return true;
        for (FragmentTransaction transaction : backstack)
            if (transaction.getMode() == TransactionMode.Push)
                return true;

        return false;
    }

    @NonNull
    private ViewGroup getContainer(FragmentState state, View root) {
        if (root == null)
            root = activity.getWindow().getDecorView().getRootView();
        View v = root.findViewById(state.layoutId);
        if (v == null)
            v = root.findViewWithTag(state.tag);
        if (v == null)
            throw new InvalidTransactionException("Unable to find layout (id: " + state.layoutId + ", tag: " + state.tag + ")");
        if (!(v instanceof ViewGroup))
            throw new InvalidTransactionException("Not a ViewGroup (id: " + state.layoutId + ", tag: " + state.tag + ")");
        for (FragmentState fs : activeStates) {
            View view = fs.getFragment().getView();
            if (view.findViewById(state.layoutId) != null)
                throw new InvalidTransactionException("Layout (id: " + state.layoutId + ", tag: " + state.tag + ") is not a child of this fragment. Use child fragment manager instead");
            if (view.findViewWithTag(state.tag) != null)
                throw new InvalidTransactionException("Layout (id: " + state.layoutId + ", tag: " + state.tag + ") is not a child of this fragment. Use child fragment manager instead");
        }
        return (ViewGroup) v;
    }

    public List<Fragment> getFragments() {
        List<Fragment> fragments = new ArrayList<>();
        synchronized (FragmentManager.class) {
            for (FragmentState state : activeStates) {
                fragments.add(state.getFragment());
            }
        }
        return fragments;
    }

    public Fragment getFragment(int id) {
        synchronized (FragmentManager.class) {
            for (FragmentState state : activeStates) {
                if (state.getFragment().getId() == id)
                    return state.getFragment();
            }
        }
        return null;
    }

    public Fragment getFragment(String tag) {
        if (tag == null)
            return null;
        synchronized (FragmentManager.class) {
            for (FragmentState state : activeStates) {
                if (tag.equals(state.getFragment().getTag()))
                    return state.getFragment();
            }
        }
        return null;
    }

    public FragmentRootView getRootView() {
        return root;
    }

    public void setRoot(FragmentRootView root) {
        this.root = root;
    }

    public void navigate(FragmentRoute route) {
        FragmentRoute.RouteStep step = route.removeStep();
        if (step.fragment == null)
            step.fragment = Fragment.instantiate(step.klass, activity);
        for (FragmentState state : activeStates) {
            if (state.getFragment().onNavigate(step.fragment, step.mode) && route.length() > 0) {
                state.getFragment().navigate(route);
                return;
            }
        }
    }

    protected void onNewIntent(Intent intent) {
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (onKeyEvent(event))
            return true;
        synchronized (FragmentManager.class) {
            List<FragmentState> copy = new ArrayList<>(activeStates);
            for (FragmentState state : copy)
                if (state.getFragment().dispatchKeyEvent(event))
                    return true;
        }
        return false;
    }

    protected boolean onKeyEvent(KeyEvent event) {
        return false;
    }

    void setFragmentManager(FragmentManager manager) {
        this.activity = manager.getActivity();
        this.fragmentManager = manager;
    }

    protected void onCreate() {
    }

    protected void onAttach() {
    }

    protected void onStart(boolean freshStart) {
    }

    protected void onResume() {
    }

    protected void onPause() {
    }

    protected void onStop() {
    }

    protected void onDetach() {
    }

    protected void onDestroy() {
    }

    protected void create(Context context) {
        this.context = context;
        id = idSequence++;
        stateMachine.update();
    }

    public void start() {
        desiredState = STATE_STARTED;
        stateMachine.update();
        synchronized (FragmentManager.class) {
            List<FragmentState> copy = new ArrayList<>(activeStates);
            for (FragmentState state : copy)
                state.getFragment().start();
        }
    }

    public void resume() {
        desiredState = STATE_RESUMED;
        stateMachine.update();
        synchronized (FragmentManager.class) {
            List<FragmentState> copy = new ArrayList<>(activeStates);
            for (FragmentState state : copy)
                state.getFragment().resume();
        }
    }

    public void pause() {
        synchronized (FragmentManager.class) {
            List<FragmentState> copy = new ArrayList<>(activeStates);
            for (FragmentState state : copy)
                state.getFragment().pause();
        }
        desiredState = STATE_STARTED;
        stateMachine.update();
    }

    public void stop() {
        synchronized (FragmentManager.class) {
            List<FragmentState> copy = new ArrayList<>(activeStates);
            for (FragmentState state : copy)
                state.getFragment().stop();
        }
        desiredState = STATE_ATTACHED;
        stateMachine.update();
    }

    public void dispatchNewIntent(Intent intent) {
        onNewIntent(intent);
        synchronized (FragmentManager.class) {
            List<FragmentState> copy = new ArrayList<>(activeStates);
            for (FragmentState state : copy)
                state.getFragment().dispatchNewIntent(intent);
        }
    }

    public void dispatchActivityResult(int requestCode, int resultCode, Intent data) {
        onActivityResult(requestCode, resultCode, data);
        synchronized (FragmentManager.class) {
            List<FragmentState> copy = new ArrayList<>(activeStates);
            for (FragmentState state : copy)
                state.getFragment().dispatchActivityResult(requestCode, resultCode, data);
        }
    }

    public void detach() {
        synchronized (FragmentManager.class) {
            List<FragmentState> copy = new ArrayList<>(activeStates);
            for (FragmentState state : copy)
                state.getFragment().detach();
        }
    }

    public void destroy() {
        synchronized (FragmentManager.class) {
            List<FragmentState> copy = new ArrayList<>(activeStates);
            for (FragmentState state : copy)
                state.getFragment().destroy();
        }
        onDestroy();
        id = -1;
        activity = null;
        fragmentManager = null;
        fresh = true;
        stateMachine.resetState();
    }

    public static Handler getHandler() {
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

    public void save(Bundle bundle) {
        List<FragmentState> allStates = new ArrayList<>();

        ArrayList<Bundle> transactionBundles = new ArrayList<>();
        for (FragmentTransaction transaction : backstack) {
            Bundle transactionBundle = new Bundle();
            transaction.save(transactionBundle, allStates);
            transactionBundles.add(transactionBundle);
        }
        bundle.putParcelableArrayList(TRANSACTIONS, transactionBundles);

        ArrayList<Bundle> stateBundles = new ArrayList<>();
        for (FragmentState state : allStates) {
            Bundle stateBundle = new Bundle();
            state.save(stateBundle);
            stateBundles.add(stateBundle);
        }
        bundle.putParcelableArrayList(STATES, stateBundles);

        int[] activeStateIndices = new int[activeStates.size()];
        for (int i = 0; i < activeStateIndices.length; i++) {
            activeStateIndices[i] = allStates.indexOf(activeStates.get(i));
        }
        bundle.putIntArray(ACTIVE_STATES, activeStateIndices);

        if (target != NO_TARGET)
            bundle.putInt(TARGET, target);
        bundle.putInt(ID, id);
        bundle.putString(TAG, tag);
        bundle.putBoolean(FRESH, fresh);

        for (int i = activeStates.size() - 1; i >= 0; i--) {
            FragmentState state = activeStates.get(i);
            removeState(state);
        }
    }

    public void restore(Bundle bundle) {
        List<FragmentState> allStates = new ArrayList<>();

        ArrayList<Bundle> stateBundles = bundle.getParcelableArrayList(STATES);
        for (Bundle stateBundle : stateBundles) {
            FragmentState state = new FragmentState();
            state.restore(stateBundle);
            allStates.add(state);
        }

        int[] activeStateIndices = bundle.getIntArray(ACTIVE_STATES);
        for (int activeStateIndice : activeStateIndices) {
            FragmentState state = allStates.get(activeStateIndice);
            startState(state);
            resumeState(state);
        }

        ArrayList<Bundle> transactionBundles = bundle.getParcelableArrayList(TRANSACTIONS);
        for (Bundle transactionBundle : transactionBundles) {
            FragmentTransaction transaction = new FragmentTransaction(this);
            transaction.restore(transactionBundle, allStates);
            backstack.add(transaction);
        }

        if (bundle.containsKey(TARGET))
            target = bundle.getInt(TARGET);
        id = bundle.getInt(ID);
        tag = bundle.getString(TAG);
        fresh = bundle.getBoolean(FRESH);
    }

    public void onResult(Bundle result) {
    }

    public void setTargetFragment(FragmentManager target) {
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

    public int getId() {
        return id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setOnStateChangeListener(OnStateChangeListener stateListener) {
        stateMachine.setOnStateChangeListener(stateListener);
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

    public StateMachine getStateMachine() {
        return stateMachine;
    }

    public boolean isCreated() {
        return stateMachine.getState() >= STATE_CREATED;
    }

    public boolean isAttached() {
        return stateMachine.getState() >= STATE_ATTACHED;
    }

    public boolean isStarted() {
        return stateMachine.getState() >= STATE_STARTED;
    }

    public boolean isResumed() {
        return stateMachine.getState() >= STATE_RESUMED;
    }
}
