package pl.zielony.fragmentmanager;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import pl.zielony.animator.Animator;
import pl.zielony.animator.AnimatorListenerAdapter;
import pl.zielony.statemachine.OnStateChangeListener;
import pl.zielony.statemachine.StateMachine;

/**
 * Created by Marcin on 2016-10-03.
 */

public abstract class ManagerBase {
    private static final String ACTIVE_STATES = ManagerBase.class.getName() + "fragmentManagerActiveStates";
    private static final String STATES = ManagerBase.class.getName() + "fragmentManagerStates";
    private static final String TRANSACTIONS = ManagerBase.class.getName() + "fragmentManagerTransactions";
    protected static final String USER_STATE = ManagerBase.class.getName() + "userState";

    public static final int STATE_CREATED = 1;
    public static final int STATE_ATTACHED = 2;
    public static final int STATE_STARTED = 3;
    public static final int STATE_RESUMED = 4;

    List<FragmentTransaction> backstack = new ArrayList<>();
    List<FragmentState> activeStates = new ArrayList<>();

    Activity activity;
    private static Handler handler = new Handler(Looper.getMainLooper());
    FragmentRootView rootView;

    private StateMachine stateMachine;
    int desiredState;

    Bundle userState;

    public ManagerBase() {
        stateMachine = new StateMachine();
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

    void startState(final FragmentState state, StateChange.Change change) {
        if (state.getFragment() == null)
            state.instantiateFragment(activity);
        Fragment fragment = state.getFragment();
        FragmentRootView rootView = fragment.getRootView();
        if (rootView.getParent() != null || fragment.getManager() != null)
            throw new IllegalStateException("Fragment (" + fragment.toString() + ") is already in use");
        final View view = fragment.getView();
        view.setVisibility(View.INVISIBLE);
        fragment.setManager(this);
        synchronized (ManagerBase.class) {
            activeStates.add(state);
        }
        ViewGroup container = getContainer(state, getRootView());
        if (change == StateChange.Change.Add) {
            container.addView(rootView);
        } else {
            container.addView(rootView, 0);
        }
        Bundle result = Fragment.getResult(fragment.getId());
        if (result != null)
            fragment.onResult(result);
        synchronized (ManagerBase.class) {
            if (isStarted())
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
            animator.addListener(fragment.getRootView().getLockListenerAdapter());
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
        synchronized (ManagerBase.class) {
            if (isResumed())
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
            animator.addListener(fragment.getRootView().getLockListenerAdapter());
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onEnd() {
                    view.setVisibility(View.INVISIBLE);
                    removeState(state);
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
        if (container == null || !activeStates.contains(state))
            throw new IllegalStateException("Fragment's container has to be in use");
        state.save();
        fragment.pause();
        fragment.stop();
        fragment.detach();
        fragment.destroy();
        synchronized (ManagerBase.class) {
            activeStates.remove(state);
        }
        state.clearFragment();
        FragmentPool.put(fragment);
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
        synchronized (ManagerBase.class) {
            for (FragmentState state : activeStates) {
                fragments.add(state.getFragment());
            }
        }
        return fragments;
    }

    public Fragment getFragment(int id) {
        synchronized (ManagerBase.class) {
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
        synchronized (ManagerBase.class) {
            for (FragmentState state : activeStates) {
                if (tag.equals(state.getFragment().getTag()))
                    return state.getFragment();
            }
        }
        return null;
    }

    public boolean navigate(FragmentRoute route) {
        FragmentRoute.Step step = route.getStep();
        if (step.fragment == null)
            step.fragment = Fragment.instantiate(step.klass, activity, null);
        if (onNavigate(route)) {
            if (route.length() == 0)
                return true;
            for (FragmentState state : activeStates) {
                if (state.getFragment().navigate(route))
                    return true;
            }
        }
        return false;
    }

    public void navigate(Fragment fragment, TransactionMode mode) {
        navigate(new FragmentRoute(fragment, mode));
    }

    public void navigate(Class<? extends Fragment> klass, TransactionMode mode) {
        navigate(new FragmentRoute(klass, mode));
    }

    protected boolean onNewIntent(Intent intent) {
        return false;
    }

    protected boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return false;
    }

    protected boolean onRequestPermissionsResult(int requestCode, List<String> granted, List<String> rejected) {
        return false;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (onKeyEvent(event))
            return true;
        synchronized (ManagerBase.class) {
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

    protected void onCreate() {
    }

    protected void onAttach() {
    }

    protected void onStart() {
    }

    protected void onResume() {
    }

    protected Bundle onSaveState() {
        return null;
    }

    protected void onPause() {
    }

    protected void onStop() {
    }

    protected void onDetach() {
    }

    protected void onDestroy() {
    }

    protected void create(Activity activity, Bundle state) {
        this.activity = activity;
        stateMachine.update();
    }

    public void start() {
        desiredState = STATE_STARTED;
        stateMachine.update();
        synchronized (ManagerBase.class) {
            List<FragmentState> copy = new ArrayList<>(activeStates);
            for (FragmentState state : copy)
                state.getFragment().start();
        }
    }

    public void resume() {
        desiredState = STATE_RESUMED;
        stateMachine.update();
        synchronized (ManagerBase.class) {
            List<FragmentState> copy = new ArrayList<>(activeStates);
            for (FragmentState state : copy)
                state.getFragment().resume();
        }
    }

    public void pause() {
        if (!isResumed())
            return;
        synchronized (ManagerBase.class) {
            List<FragmentState> copy = new ArrayList<>(activeStates);
            for (FragmentState state : copy)
                state.getFragment().pause();
        }
        desiredState = STATE_STARTED;
        stateMachine.update();
    }

    public void stop() {
        if (!isStarted())
            return;
        synchronized (ManagerBase.class) {
            List<FragmentState> copy = new ArrayList<>(activeStates);
            for (FragmentState state : copy)
                state.getFragment().stop();
        }
        desiredState = STATE_ATTACHED;
        stateMachine.update();
    }

    public void detach() {
        if (!isAttached())
            return;
        synchronized (ManagerBase.class) {
            List<FragmentState> copy = new ArrayList<>(activeStates);
            for (FragmentState state : copy)
                state.getFragment().detach();
        }
        ((ViewGroup) getRootView().getParent()).removeView(getRootView());
    }

    public void dispatchNewIntent(Intent intent) {
        onNewIntent(intent);
        synchronized (ManagerBase.class) {
            List<FragmentState> copy = new ArrayList<>(activeStates);
            for (FragmentState state : copy)
                state.getFragment().dispatchNewIntent(intent);
        }
    }

    public void dispatchActivityResult(int requestCode, int resultCode, Intent data) {
        if (onActivityResult(requestCode, resultCode, data))
            return;
        synchronized (ManagerBase.class) {
            List<FragmentState> copy = new ArrayList<>(activeStates);
            for (FragmentState state : copy)
                state.getFragment().dispatchActivityResult(requestCode, resultCode, data);
        }
    }

    public void dispatchRequestPermissionsResult(int requestCode, List<String> granted, List<String> rejected) {
        if (onRequestPermissionsResult(requestCode, granted, rejected))
            return;
        synchronized (ManagerBase.class) {
            List<FragmentState> copy = new ArrayList<>(activeStates);
            for (FragmentState state : copy)
                state.getFragment().dispatchRequestPermissionsResult(requestCode, granted, rejected);
        }
    }


    public void destroy() {
        if (!isCreated())
            return;
        synchronized (ManagerBase.class) {
            List<FragmentState> copy = new ArrayList<>(activeStates);
            for (FragmentState state : copy)
                state.getFragment().destroy();
        }
        onDestroy();
        activity = null;
        activeStates.clear();
        backstack.clear();
        stateMachine.resetState();
    }

    public static Handler getHandler() {
        return handler;
    }

    public Activity getActivity() {
        return activity;
    }

    /**
     * Useful when your activity is really an instance of FragmentActivity
     *
     * @return
     */
    public FragmentActivity getFragmentActivity() {
        return (FragmentActivity) activity;
    }

    public Resources getResources() {
        return activity.getResources();
    }

    public Object getSystemService(String serviceName) {
        return activity.getSystemService(serviceName);
    }

    public Bundle getState() {
        return userState;
    }

    @NonNull
    public FragmentRootView getRootView() {
        return rootView;
    }

    public void save(Bundle state) {
        List<FragmentState> allStates = new ArrayList<>();

        ArrayList<Bundle> transactionBundles = new ArrayList<>();
        for (FragmentTransaction transaction : backstack) {
            Bundle transactionBundle = new Bundle();
            transaction.save(transactionBundle, allStates);
            transactionBundles.add(transactionBundle);
        }
        state.putParcelableArrayList(TRANSACTIONS, transactionBundles);

        ArrayList<Bundle> stateBundles = new ArrayList<>();
        for (FragmentState fragmentState : allStates) {
            Bundle stateBundle = fragmentState.save();
            stateBundles.add(stateBundle);
        }
        state.putParcelableArrayList(STATES, stateBundles);

        int[] activeStateIndices = new int[activeStates.size()];
        for (int i = 0; i < activeStateIndices.length; i++) {
            activeStateIndices[i] = allStates.indexOf(activeStates.get(i));
        }
        state.putIntArray(ACTIVE_STATES, activeStateIndices);

        userState = onSaveState();
        state.putBundle(USER_STATE, userState != null ? userState : new Bundle());
    }

    public void restore(Bundle state) {
        List<FragmentState> allStates = new ArrayList<>();

        ArrayList<Bundle> stateBundles = state.getParcelableArrayList(STATES);
        for (Bundle stateBundle : stateBundles) {
            FragmentState fragmentState = new FragmentState();
            fragmentState.restore(stateBundle);
            allStates.add(fragmentState);
        }

        int[] activeStateIndices = state.getIntArray(ACTIVE_STATES);
        for (int activeStateIndice : activeStateIndices) {
            FragmentState fragmentState = allStates.get(activeStateIndice);
            startState(fragmentState, StateChange.Change.Add);
            fragmentState.getFragment().getView().setVisibility(View.VISIBLE);
            resumeState(fragmentState);
        }

        ArrayList<Bundle> transactionBundles = state.getParcelableArrayList(TRANSACTIONS);
        for (Bundle transactionBundle : transactionBundles) {
            FragmentTransaction transaction = new FragmentTransaction(this);
            transaction.restore(transactionBundle, allStates);
            backstack.add(transaction);
        }
    }

    public String getString(int resId) {
        return activity.getString(resId);
    }

    public String getString(int resId, Object... args) {
        return activity.getString(resId, args);
    }

    public void setOnStateChangeListener(OnStateChangeListener stateListener) {
        stateMachine.setOnStateChangeListener(stateListener);
    }

    public boolean onNavigate(FragmentRoute route) {
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
