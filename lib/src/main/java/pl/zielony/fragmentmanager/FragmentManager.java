package pl.zielony.fragmentmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marcin on 2015-03-20.
 */
public class FragmentManager {
    private static final String ACTIVE_STATES = FragmentManager.class.getName() + "fragmentManagerActiveStates";
    private static final String STATES = FragmentManager.class.getName() + "fragmentManagerStates";
    private static final String TRANSACTIONS = FragmentManager.class.getName() + "fragmentManagerTransactions";

    private final Activity activity;
    List<FragmentTransaction> backstack = new ArrayList<>();
    private List<FragmentState> activeStates = new ArrayList<>();
    private FragmentManager parent;
    private FragmentRootView root;
    private boolean started = false;
    private boolean resumed = false;

    public FragmentManager(Activity activity) {
        this.activity = activity;
    }

    public FragmentManager(FragmentManager parent) {
        this.activity = parent.getActivity();
        this.parent = parent;
    }

    // -------------------
    // add
    // -------------------

    public <T extends Fragment> FragmentTransaction add(T fragment, int id, TransactionMode mode) {
        return addFragment(fragment, id, null, mode);
    }

    public <T extends Fragment> FragmentTransaction add(T fragment, String tag, TransactionMode mode) {
        return addFragment(fragment, 0, tag, mode);
    }

    public <T extends Fragment> FragmentTransaction add(Class<T> fragmentClass, int id, TransactionMode mode) {
        T fragment = Fragment.instantiate(fragmentClass, activity);
        return add(fragment, id, mode);
    }

    public <T extends Fragment> FragmentTransaction add(Class<T> fragmentClass, String tag, TransactionMode mode) {
        T fragment = Fragment.instantiate(fragmentClass, activity);
        return add(fragment, tag, mode);
    }

    private FragmentTransaction addFragment(Fragment fragment, final int id, String tag, TransactionMode mode) {
        FragmentTransaction transaction = new FragmentTransaction(this, mode);

        transaction.addStateChange(new FragmentState(fragment, id, tag), FragmentTransaction.StateChange.Change.Add);
        return transaction;
    }

    // -------------------
    // replace
    // -------------------

    public <T extends Fragment> FragmentTransaction replace(T addFragment, int id, TransactionMode mode) {
        return replaceFragment(null, addFragment, id, null, mode);
    }

    public <T extends Fragment> FragmentTransaction replace(T addFragment, String tag, TransactionMode mode) {
        return replaceFragment(null, addFragment, 0, tag, mode);
    }

    public <T extends Fragment, T2 extends Fragment> FragmentTransaction replace(T2 removeFragment, T addFragment, TransactionMode mode) {
        return replaceFragment(removeFragment, addFragment, 0, null, mode);
    }

    public <T extends Fragment> FragmentTransaction replace(Class<T> fragmentClass, int id, TransactionMode mode) {
        T fragment = Fragment.instantiate(fragmentClass, activity);
        return replace(fragment, id, mode);
    }

    public <T extends Fragment> FragmentTransaction replace(Class<T> fragmentClass, String tag, TransactionMode mode) {
        T fragment = Fragment.instantiate(fragmentClass, activity);
        return replace(fragment, tag, mode);
    }

    public <T extends Fragment, T2 extends Fragment> FragmentTransaction replace(T2 removeFragment, Class<T> fragmentClass, TransactionMode mode) {
        T fragment = Fragment.instantiate(fragmentClass, activity);
        return replace(removeFragment, fragment, mode);
    }

    private FragmentTransaction replaceFragment(Fragment removeFragment, Fragment fragment, int id, String tag, TransactionMode mode) {
        FragmentTransaction transaction = new FragmentTransaction(this, mode);

        for (int i = activeStates.size() - 1; i >= 0; i--) {
            final FragmentState state = activeStates.get(i);
            if (state.getFragment() == removeFragment || state.layoutId == id || tag != null && tag.equals(state.tag)) {
                if (mode == TransactionMode.Join)
                    removeStateFromBackstack(state);
                transaction.addStateChange(state, FragmentTransaction.StateChange.Change.Remove);
                transaction.addStateChange(new FragmentState(fragment, state.layoutId, state.tag), FragmentTransaction.StateChange.Change.Add);
                break;
            }
        }

        return transaction;
    }

    private void removeStateFromBackstack(FragmentState state) {
        for (FragmentTransaction transaction : backstack) {
            for (FragmentTransaction.StateChange change : transaction.changes) {
                // it has to be that one state
                if (change.getState() == state) {
                    transaction.changes.remove(change);
                    break;
                }
            }
        }
    }

    // -------------------
    // remove
    // -------------------

    public FragmentTransaction remove(int id, TransactionMode mode) {
        return removeFragment(null, id, null, mode);
    }

    public FragmentTransaction remove(String tag, TransactionMode mode) {
        return removeFragment(null, 0, tag, mode);
    }

    public <T extends Fragment> FragmentTransaction remove(T fragment, TransactionMode mode) {
        return removeFragment(fragment, 0, null, mode);
    }

    private FragmentTransaction removeFragment(Fragment removeFragment, int id, String tag, TransactionMode mode) {
        FragmentTransaction transaction = new FragmentTransaction(this, mode);

        for (int i = activeStates.size() - 1; i >= 0; i--) {
            final FragmentState state = activeStates.get(i);
            if (state.getFragment() == removeFragment || state.layoutId == id || tag != null && tag.equals(state.tag)) {
                if (mode == TransactionMode.Join)
                    removeStateFromBackstack(state);
                transaction.addStateChange(new FragmentState(state.getFragment(), state.layoutId, state.tag), FragmentTransaction.StateChange.Change.Remove);
                break;
            }
        }

        return transaction;
    }

    public boolean upTraverse() {
        for (int i = activeStates.size() - 1; i >= 0; i--) {
            if (activeStates.get(i).getFragment().getChildFragmentManager().up())
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
            if (activeStates.get(i).getFragment().getChildFragmentManager().up())
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
            if (activeStates.get(i).getFragment().getChildFragmentManager().back())
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

    Animator addState(final FragmentState state) {
        if (state.getFragment() == null)
            state.instantiateFragment(activity);
        Fragment fragment = state.getFragment();
        fragment.setFragmentManager(this);
        synchronized (FragmentManager.class) {
            activeStates.add(state);
        }
        FragmentRootView rootView = fragment.getRootView();
        final ViewGroup container = getContainer(state, root);
        container.addView(rootView);
        synchronized (FragmentManager.class) {
            if (started)
                fragment.start();
        }
        final View view = fragment.getView();
        view.setVisibility(View.INVISIBLE);
        Animator animator = fragment.animateAdd();
        if (animator != null) {
            animator.addListener(new LockListenerAdapter(rootView));
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    view.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    animateAddFinished(state);
                }
            });
        } else {
            animateAddFinished(state);
        }
        return animator;
    }

    private void animateAddFinished(FragmentState state) {
        synchronized (FragmentManager.class) {
            if (resumed)
                state.getFragment().resume();
        }
    }

    Animator startState(final FragmentState state) {
        if (state.getFragment() == null)
            state.instantiateFragment(activity);
        Fragment fragment = state.getFragment();
        fragment.setFragmentManager(this);
        synchronized (FragmentManager.class) {
            activeStates.add(state);
        }
        ViewGroup container = getContainer(state, root);
        FragmentRootView rootView = fragment.getRootView();
        container.addView(rootView, 0);
        Bundle fragmentState = state.getFragmentState();
        if (!fragmentState.isEmpty()) {
            fragment.onRestoreState(fragmentState);
            fragmentState.clear();
        }
        Bundle result = Fragment.getResult(fragment.getId());
        if (result != null)
            fragment.onResult(result);
        synchronized (FragmentManager.class) {
            if (started)
                fragment.start();
        }
        final View view = fragment.getView();
        view.setVisibility(View.INVISIBLE);
        ViewHelper.setAlpha(view, 1);
        Animator animator = fragment.animateStart();
        if (animator != null) {
            animator.addListener(new LockListenerAdapter(rootView));
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    view.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    animateStartFinished(state);
                }
            });
        } else {
            animateStartFinished(state);
        }
        return animator;
    }

    private void animateStartFinished(FragmentState state) {
        synchronized (FragmentManager.class) {
            if (resumed)
                state.getFragment().resume();
        }
    }

    Animator stopState(final FragmentState state) {
        final Fragment fragment = state.getFragment();
        final FragmentRootView view = fragment.getRootView();
        final ViewGroup container = getContainer(state, root);
        synchronized (FragmentManager.class) {
            activeStates.remove(state);
        }
        Animator animator = fragment.animateStop();
        if (animator != null) {
            animator.addListener(new LockListenerAdapter(view));
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    animateStopFinished(state, container);
                }
            });
        } else {
            animateStopFinished(state, container);
        }
        return animator;
    }

    private void animateStopFinished(FragmentState state, ViewGroup container) {
        final Fragment fragment = state.getFragment();
        final View rootView = fragment.getRootView();
        fragment.pause();
        View view = fragment.getView();
        view.setVisibility(View.INVISIBLE);
        fragment.stop();
        view.setAnimation(null);
        container.removeView(rootView);
        state.clearFragment();
        fragment.onSaveState(state.getFragmentState());
        fragment.clear();
        Fragment.pool(fragment);
    }

    Animator removeState(final FragmentState state) {
        final Fragment fragment = state.getFragment();
        final FragmentRootView view = fragment.getRootView();
        final ViewGroup container = getContainer(state, root);
        synchronized (FragmentManager.class) {
            activeStates.remove(state);
        }
        Animator animator = fragment.animateRemove();
        if (animator != null) {
            animator.addListener(new LockListenerAdapter(view));
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    animateRemoveFinished(state, container);
                }
            });
        } else {
            animateRemoveFinished(state, container);
        }
        return animator;
    }

    private void animateRemoveFinished(FragmentState state, ViewGroup container) {
        final Fragment fragment = state.getFragment();
        fragment.pause();
        final View view = fragment.getRootView();
        container.removeView(view);
        state.clearFragment();
        fragment.stop();
        fragment.clear();
        Fragment.pool(fragment);
    }

    public boolean hasBack() {
        for (FragmentState state : activeStates)
            if (state.getFragment().getChildFragmentManager().hasBack())
                return true;
        for (FragmentTransaction transaction : backstack)
            if (transaction.getMode() != TransactionMode.Join)
                return true;

        return false;
    }

    public boolean hasUp() {
        for (FragmentState state : activeStates)
            if (state.getFragment().getChildFragmentManager().hasUp())
                return true;
        for (FragmentTransaction transaction : backstack)
            if (transaction.getMode() == TransactionMode.Push)
                return true;

        return false;
    }

    public void save(Bundle bundle) {
        List<FragmentState> allStates = new ArrayList<>();

        for (int i = activeStates.size() - 1; i >= 0; i--) {
            FragmentState state = activeStates.get(i);
            if (resumed)
                state.getFragment().pause();
            if (started)
                state.getFragment().stop();
            state.getFragment().onSaveState(state.getFragmentState());
            //getContainer(state).removeView(state.getFragment().getRootView());
        }

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

            if (state.getFragment() == null)
                state.instantiateFragment(activity);
            Fragment fragment = state.getFragment();
            fragment.setFragmentManager(this);
            synchronized (FragmentManager.class) {
                activeStates.add(state);
            }
            ViewGroup container = getContainer(state, root);
            View view = fragment.getRootView();
            container.addView(view);
            if (!state.getFragmentState().isEmpty())
                fragment.onRestoreState(state.getFragmentState());
            synchronized (FragmentManager.class) {
                if (started)
                    fragment.start();
                if (resumed)
                    fragment.resume();
            }
        }

        ArrayList<Bundle> transactionBundles = bundle.getParcelableArrayList(TRANSACTIONS);
        for (Bundle transactionBundle : transactionBundles) {
            FragmentTransaction transaction = new FragmentTransaction(this);
            transaction.restore(transactionBundle, allStates);
            backstack.add(transaction);
        }
    }

    @NonNull
    private ViewGroup getContainer(FragmentState transaction, View root) {
        if (root == null)
            root = activity.getWindow().getDecorView().getRootView();
        View v = root.findViewById(transaction.layoutId);
        if (v == null)
            v = root.findViewWithTag(transaction.tag);
        if (v == null)
            throw new InvalidTransactionException("Unable to find layout (id: " + transaction.layoutId + ", tag: " + transaction.tag + ")");
        if (!(v instanceof ViewGroup))
            throw new InvalidTransactionException("Not a ViewGroup (id: " + transaction.layoutId + ", tag: " + transaction.tag + ")");
        return (ViewGroup) v;
    }

    public Activity getActivity() {
        return activity;
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

    public FragmentManager getParent() {
        return parent;
    }

    public FragmentRootView getRootView() {
        return root;
    }

    public void setRoot(FragmentRootView root) {
        this.root = root;
    }

    public void onStart() {
        synchronized (FragmentManager.class) {
            started = true;
            List<FragmentState> copy = new ArrayList<>(activeStates);
            for (FragmentState state : copy)
                state.getFragment().start();
        }
    }

    public void onResume() {
        synchronized (FragmentManager.class) {
            resumed = true;
            List<FragmentState> copy = new ArrayList<>(activeStates);
            for (FragmentState state : copy)
                state.getFragment().resume();
        }
    }

    public void onPause() {
        synchronized (FragmentManager.class) {
            resumed = false;
            List<FragmentState> copy = new ArrayList<>(activeStates);
            for (FragmentState state : copy)
                state.getFragment().pause();
        }
    }

    public void onStop() {
        synchronized (FragmentManager.class) {
            started = false;
            List<FragmentState> copy = new ArrayList<>(activeStates);
            for (FragmentState state : copy)
                state.getFragment().stop();
        }
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isResumed() {
        return resumed;
    }

    public void onNewIntent(Intent intent) {
        synchronized (FragmentManager.class) {
            List<FragmentState> copy = new ArrayList<>(activeStates);
            for (FragmentState state : copy)
                state.getFragment().onNewIntent(intent);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        synchronized (FragmentManager.class) {
            List<FragmentState> copy = new ArrayList<>(activeStates);
            for (FragmentState state : copy)
                state.getFragment().onActivityResult(requestCode, resultCode, data);
        }
    }

    public boolean onKeyEvent(KeyEvent event) {
        synchronized (FragmentManager.class) {
            List<FragmentState> copy = new ArrayList<>(activeStates);
            for (FragmentState state : copy)
                if (state.getFragment().onKeyEvent(event))
                    return true;
        }
        return false;
    }
}
