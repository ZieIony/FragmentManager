package pl.zielony.fragmentmanager;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Marcin on 2015-03-20.
 */
public class FragmentManager implements FragmentManagerInterface {
    private static final String ACTIVE_STATES = FragmentManager.class.getName() + "fragmentManagerActiveStates";
    private static final String STATES = FragmentManager.class.getName() + "fragmentManagerStates";
    private static final String TRANSACTIONS = FragmentManager.class.getName() + "fragmentManagerTransactions";

    private final Activity activity;
    List<FragmentTransaction> backstack = new ArrayList<>();
    private List<FragmentState> activeStates = new ArrayList<>();
    private static HashMap<Integer, Bundle> results = new HashMap<>();
    private boolean restoring = false;
    private FragmentManager parent;
    private ViewGroup root;

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

    public <T extends Fragment> T add(T fragment, int id, FragmentTransaction.Mode mode) {
        addFragment(fragment, id, null, mode);
        return fragment;
    }

    public <T extends Fragment> T add(T fragment, String tag, FragmentTransaction.Mode mode) {
        addFragment(fragment, 0, tag, mode);
        return fragment;
    }

    public <T extends Fragment> T add(Class<T> fragmentClass, int id, FragmentTransaction.Mode mode) {
        T fragment = instantiate(fragmentClass);
        return add(fragment, id, mode);
    }

    public <T extends Fragment> T add(Class<T> fragmentClass, String tag, FragmentTransaction.Mode mode) {
        T fragment = instantiate(fragmentClass);
        return add(fragment, tag, mode);
    }

    private void addFragment(Fragment fragment, final int id, String tag, FragmentTransaction.Mode mode) {
        FragmentTransaction transaction = new FragmentTransaction(this, mode);

        transaction.addStateChange(new FragmentState(fragment, id, tag), FragmentTransaction.StateChange.Change.Add);
        transaction.execute();
    }

    // -------------------
    // replace
    // -------------------

    public <T extends Fragment> T replace(T addFragment, int id, FragmentTransaction.Mode mode) {
        replaceFragment(null, addFragment, id, null, mode);
        return addFragment;
    }

    public <T extends Fragment> T replace(T addFragment, String tag, FragmentTransaction.Mode mode) {
        replaceFragment(null, addFragment, 0, tag, mode);
        return addFragment;
    }

    public <T extends Fragment, T2 extends Fragment> T replace(T2 removeFragment, T addFragment, FragmentTransaction.Mode mode) {
        replaceFragment(removeFragment, addFragment, 0, null, mode);
        return addFragment;
    }

    public <T extends Fragment> T replace(Class<T> fragmentClass, int id, FragmentTransaction.Mode mode) {
        T fragment = instantiate(fragmentClass);
        return replace(fragment, id, mode);
    }

    public <T extends Fragment> T replace(Class<T> fragmentClass, String tag, FragmentTransaction.Mode mode) {
        T fragment = instantiate(fragmentClass);
        return replace(fragment, tag, mode);
    }

    public <T extends Fragment, T2 extends Fragment> T replace(T2 removeFragment, Class<T> fragmentClass, FragmentTransaction.Mode mode) {
        T fragment = instantiate(fragmentClass);
        return replace(removeFragment, fragment, mode);
    }

    private void replaceFragment(Fragment removeFragment, Fragment fragment, int id, String tag, FragmentTransaction.Mode mode) {
        FragmentTransaction transaction = new FragmentTransaction(this, mode);

        for (int i = activeStates.size() - 1; i >= 0; i--) {
            final FragmentState state = activeStates.get(i);
            if (state.fragment == removeFragment || state.layoutId == id || tag != null && tag.equals(state.tag)) {
                if (mode == FragmentTransaction.Mode.Join)
                    removeStateFromBackstack(state);
                transaction.addStateChange(state, FragmentTransaction.StateChange.Change.Remove);
                transaction.addStateChange(new FragmentState(fragment, state.layoutId, state.tag), FragmentTransaction.StateChange.Change.Add);
                break;
            }
        }

        transaction.execute();
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

    public void remove(int id, FragmentTransaction.Mode mode) {
        removeFragment(null, id, null, mode);
    }

    public void remove(String tag, FragmentTransaction.Mode mode) {
        removeFragment(null, 0, tag, mode);
    }

    public <T extends Fragment> void remove(T fragment, FragmentTransaction.Mode mode) {
        removeFragment(fragment, 0, null, mode);
    }

    private void removeFragment(Fragment removeFragment, int id, String tag, FragmentTransaction.Mode mode) {
        FragmentTransaction transaction = new FragmentTransaction(this, mode);

        for (int i = activeStates.size() - 1; i >= 0; i--) {
            final FragmentState state = activeStates.get(i);
            if (state.fragment == removeFragment || state.layoutId == id || tag != null && tag.equals(state.tag)) {
                if (mode == FragmentTransaction.Mode.Join)
                    removeStateFromBackstack(state);
                transaction.addStateChange(new FragmentState(state.fragment, state.layoutId, state.tag), FragmentTransaction.StateChange.Change.Remove);
                break;
            }
        }

        transaction.execute();
    }

    public boolean up() {
        for (int i = activeStates.size() - 1; i >= 0; i--) {
            //if (activeStates.get(i).fragment.hasUp())
            if (activeStates.get(i).fragment.up())
                return true;
        }
        if (!hasUp())
            return false;
        while (backstack.size() != 0) {
            FragmentTransaction transaction = backstack.remove(backstack.size() - 1);
            FragmentTransaction.Mode mode = transaction.getMode();
            transaction.undo();
            if (mode == FragmentTransaction.Mode.Push)
                return true;
        }
        return false;
    }

    /**
     * Pops one backstack step
     *
     * @return if back could pop one complete step
     */
    public boolean back() {
        for (int i = activeStates.size() - 1; i >= 0; i--) {
            //if (activeStates.get(i).fragment.hasBack())
            if (activeStates.get(i).fragment.back())
                return true;
        }
        if (!hasBack())
            return false;
        while (backstack.size() != 0) {
            FragmentTransaction transaction = backstack.remove(backstack.size() - 1);
            FragmentTransaction.Mode mode = transaction.getMode();
            transaction.undo();
            if (mode != FragmentTransaction.Mode.Join)
                return true;
        }
        return false;
    }

    void inAddState(FragmentState state) {
        if (state.fragment == null)
            state.fragment = instantiate(state.fragmentClass);
        Fragment fragment = state.fragment;
        activeStates.add(state);
        View view = fragment.getView();
        view.setVisibility(View.INVISIBLE);
        ViewGroup container = getContainer(state, root);
        container.addView(view);
        if (view instanceof ViewGroup)
            fragment.getChildFragmentManager().setRoot((ViewGroup) view);
        fragment.start();
    }

    void inAddStateAnimate(FragmentState state) {
        Fragment fragment = state.fragment;
        fragment.getView().setVisibility(View.VISIBLE);
        fragment.animateInAdd();
    }

    void inBackState(FragmentState state) {
        boolean prevRestoring = restoring;
        restoring = true;

        if (state.fragment == null)
            state.fragment = instantiate(state.fragmentClass);
        Fragment fragment = state.fragment;
        activeStates.add(state);
        ViewGroup container = getContainer(state, root);
        View view = fragment.getView();
        container.addView(view, 0);
        if (view instanceof ViewGroup)
            fragment.getChildFragmentManager().setRoot((ViewGroup) view);
        if (!state.fragmentState.isEmpty()) {
            fragment.onRestoreState(state.fragmentState);
            state.fragmentState.clear();
        }
        if (results.containsKey(fragment.getId())) {
            fragment.onResult(results.get(fragment.getId()));
            results.remove(fragment.getId());
        }
        fragment.resume();

        restoring = prevRestoring;
    }

    void inBackStateAnimate(FragmentState state) {
        Fragment fragment = state.fragment;
        fragment.getView().setVisibility(View.VISIBLE);
        fragment.animateInBack();
    }

    void outAddStateAnimate(final FragmentState state) {
        final Fragment fragment = state.fragment;
        final ViewGroup container = getContainer(state, root);
        final View view = fragment.getView();
        activeStates.remove(state);
        fragment.animateOutAdd(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fragment.pause();
                view.setVisibility(View.INVISIBLE);
                fragment.onSaveState(state.fragmentState);
                view.setAnimation(null);
                container.removeView(view);
                fragment.getChildFragmentManager().setRoot(null);
                state.fragment = null;
            }
        });
    }

    void outBackStateAnimate(final FragmentState state) {
        final Fragment fragment = state.fragment;
        final ViewGroup container = getContainer(state, root);
        final View view = fragment.getView();
        activeStates.remove(state);
        fragment.animateOutBack(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fragment.finish();
                view.setVisibility(View.INVISIBLE);
                view.setAnimation(null);
                container.removeView(view);
                fragment.getChildFragmentManager().setRoot(null);
                state.fragment = null;
            }
        });
    }

    public boolean hasBack() {
        for (FragmentState state : activeStates)
            if (state.fragment.getChildFragmentManager().hasBack())
                return true;
        for (FragmentTransaction transaction : backstack)
            if (transaction.getMode() != FragmentTransaction.Mode.Join)
                return true;

        return false;
    }

    public boolean hasUp() {
        for (FragmentState state : activeStates)
            if (state.fragment.getChildFragmentManager().hasUp())
                return true;
        for (FragmentTransaction transaction : backstack)
            if (transaction.getMode() == FragmentTransaction.Mode.Push)
                return true;

        return false;
    }

    public void clear() {
        backstack.clear();
        activeStates.clear();
    }

    public void save(Bundle bundle) {
        List<FragmentState> allStates = new ArrayList<>();

        for (int i = activeStates.size() - 1; i >= 0; i--) {
            FragmentState state = activeStates.get(i);
            //state.fragment.onPause();
            state.fragment.onSaveState(state.fragmentState);
            //getContainer(state).removeView(state.fragment.getView());
        }

        ArrayList<Bundle> transactionBundles = new ArrayList<>();
        for (FragmentTransaction transaction : backstack) {
            Bundle transactionBundle = new Bundle();
            transaction.save(transactionBundle, allStates);
            transactionBundles.add(transactionBundle);
        }

        ArrayList<Bundle> stateBundles = new ArrayList<>();
        for (FragmentState state : allStates) {
            Bundle stateBundle = new Bundle();
            state.save(stateBundle);
            stateBundles.add(stateBundle);
        }

        int[] activeStateIndices = new int[activeStates.size()];
        for (int i = 0; i < activeStateIndices.length; i++) {
            activeStateIndices[i] = allStates.indexOf(activeStates.get(i));
        }

        bundle.putIntArray(ACTIVE_STATES, activeStateIndices);
        bundle.putParcelableArrayList(STATES, stateBundles);
        bundle.putParcelableArrayList(TRANSACTIONS, transactionBundles);
    }

    public void restore(Bundle bundle) {
        restoring = true;

        List<FragmentState> allStates = new ArrayList<>();

        clear();
        int[] activeStateIndices = bundle.getIntArray(ACTIVE_STATES);
        bundle.remove(ACTIVE_STATES);
        ArrayList<Bundle> stateBundles = bundle.getParcelableArrayList(STATES);
        bundle.remove(STATES);
        ArrayList<Bundle> transactionBundles = bundle.getParcelableArrayList(TRANSACTIONS);
        bundle.remove(TRANSACTIONS);

        for (Bundle stateBundle : stateBundles) {
            FragmentState state = new FragmentState();
            state.restore(stateBundle);
            allStates.add(state);
        }

        for (int activeStateIndice : activeStateIndices) {
            FragmentState state = allStates.get(activeStateIndice);

            if (state.fragment == null)
                state.fragment = instantiate(state.fragmentClass);
            Fragment fragment = state.fragment;
            activeStates.add(state);
            ViewGroup container = getContainer(state, root);
            View view = fragment.getView();
            container.addView(view);
            if (view instanceof ViewGroup)
                fragment.getChildFragmentManager().setRoot((ViewGroup) view);
            if (!state.fragmentState.isEmpty())
                fragment.onRestoreState(state.fragmentState);
            fragment.resume();
        }

        for (Bundle transactionBundle : transactionBundles) {
            FragmentTransaction transaction = new FragmentTransaction(this);
            transaction.restore(transactionBundle, allStates);
            backstack.add(transaction);
        }

        restoring = false;
    }

    public boolean isRestoring() {
        return restoring;
    }

    @NonNull
    private ViewGroup getContainer(FragmentState transaction, View root) {
        if (root == null)
            root = activity.getWindow().getDecorView().getRootView();
        View v = root.findViewById(transaction.layoutId);
        if (v != null)
            return (ViewGroup) v;
        v = root.findViewWithTag(transaction.tag);
        if (v != null)
            return (ViewGroup) v;
        throw new RuntimeException("Unable to find layout id: " + transaction.layoutId + ", tag: " + transaction.tag);
    }

    private <T extends Fragment> T instantiate(Class<T> fragmentClass) {
        Fragment fragment;
        try {
            fragment = fragmentClass.getConstructor(FragmentManager.class).newInstance(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return (T) fragment;
    }

    public Activity getActivity() {
        return activity;
    }

    public List<Fragment> getFragments() {
        List<Fragment> fragments = new ArrayList<>();
        for (FragmentState state : activeStates) {
            fragments.add(state.fragment);
        }
        return fragments;
    }

    public Fragment getFragment(int id) {
        for (FragmentState state : activeStates) {
            if (state.fragment.getId() == id)
                return state.fragment;
        }
        return null;
    }

    public Fragment getFragment(String tag) {
        if (tag == null)
            return null;
        for (FragmentState state : activeStates) {
            if (tag.equals(state.fragment.getTag()))
                return state.fragment;
        }
        return null;
    }

    public FragmentManager getParent() {
        return parent;
    }

    public void setResult(int id, Bundle result) {
        results.put(id, result);
    }

    public ViewGroup getRoot() {
        return root;
    }

    public void setRoot(ViewGroup root) {
        this.root = root;
    }
}
