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
    private final Activity activity;
    List<FragmentTransaction> backstack = new ArrayList<>();
    List<FragmentState> activeStates = new ArrayList<>();
    static HashMap<Integer, Bundle> results = new HashMap<>();
    boolean restoring = false;
    FragmentManager parent;

    public FragmentManager(Activity activity) {
        this.activity = activity;
    }

    public FragmentManager(FragmentManager parent) {
        this.activity = parent.getActivity();
        this.parent = parent;
    }

    @Override
    public <T extends Fragment> T add(T fragment, int id, FragmentState.Mode mode) {
        return addFragment(fragment, id, null, mode);
    }

    @Override
    public <T extends Fragment> T add(T fragment, String tag, FragmentState.Mode mode) {
        return addFragment(fragment, 0, tag, mode);
    }

    @Override
    public <T extends Fragment> T add(Class<T> fragmentClass, int id, FragmentState.Mode mode) {
        T fragment = instantiate(fragmentClass);
        return addFragment(fragment, id, null, mode);
    }

    @Override
    public <T extends Fragment> T add(Class<T> fragmentClass, String tag, FragmentState.Mode mode) {
        T fragment = instantiate(fragmentClass);
        return addFragment(fragment, 0, tag, mode);
    }

    @Override
    public <T extends Fragment> T replace(T fragment, int id, FragmentState.Mode mode) {
        return replaceFragment(fragment, id, null, mode);
    }

    @Override
    public <T extends Fragment> T replace(T fragment, String tag, FragmentState.Mode mode) {
        return replaceFragment(fragment, 0, tag, mode);
    }

    @Override
    public <T extends Fragment> T replace(Class<T> fragmentClass, int id, FragmentState.Mode mode) {
        T fragment = instantiate(fragmentClass);
        return replaceFragment(fragment, id, null, mode);
    }

    @Override
    public <T extends Fragment> T replace(Class<T> fragmentClass, String tag, FragmentState.Mode mode) {
        T fragment = instantiate(fragmentClass);
        return replaceFragment(fragment, 0, tag, mode);
    }

    @Override
    public <T extends Fragment> T remove(T fragment, int id, FragmentState.Mode mode) {
        return removeFragment(fragment, id, null, mode);
    }

    @Override
    public <T extends Fragment> T remove(T fragment, String tag, FragmentState.Mode mode) {
        return removeFragment(fragment, 0, tag, mode);
    }

    private <T extends Fragment> T addFragment(T fragment, final int id, String tag, FragmentState.Mode mode) {
        FragmentTransaction transaction = new FragmentTransaction(this);

        transaction.addStateChange(new FragmentState(fragment, id, tag, mode), FragmentTransaction.Mode.Add);
        backstack.add(transaction);
        transaction.execute();

        return (T) fragment;
    }


    private <T extends Fragment> T replaceFragment(T fragment, final int id, String tag, FragmentState.Mode mode) {
        FragmentTransaction transaction = new FragmentTransaction(this);

        if (mode != FragmentState.Mode.Dialog) {
            for (int i = activeStates.size() - 1; i >= 0; i--) {
                final FragmentState state = activeStates.get(i);
                if (state.layoutId == id || tag != null && tag.equals(state.tag))
                    transaction.addStateChange(state, FragmentTransaction.Mode.Remove);
            }
        }

        transaction.addStateChange(new FragmentState(fragment, id, tag, mode), FragmentTransaction.Mode.Add);
        backstack.add(transaction);
        transaction.execute();

        return (T) fragment;
    }

    private <T extends Fragment> T removeFragment(T fragment, int id, String tag, FragmentState.Mode mode) {
        FragmentTransaction transaction = new FragmentTransaction(this);

        if (mode != FragmentState.Mode.Dialog) {
            for (int i = activeStates.size() - 1; i >= 0; i--) {
                final FragmentState state = activeStates.get(i);
                if (state.layoutId == id || tag != null && tag.equals(state.tag))
                    transaction.addStateChange(state, FragmentTransaction.Mode.Remove);
            }
        }

        backstack.add(transaction);
        transaction.execute();

        return fragment;
    }

    private void cleanJoins() {

    }

    public boolean up() {
        for (int i = activeStates.size() - 1; i >= 0; i--) {
            if (activeStates.get(i).fragment.hasUp())
                if (activeStates.get(i).fragment.up())
                    return true;
        }
        while (backstack.size() != 0) {
            FragmentState.Mode mode = activeStates.get(activeStates.size() - 1).mode;
            FragmentTransaction transaction = backstack.remove(backstack.size() - 1);
            transaction.undo();
            if (mode == FragmentState.Mode.Push)
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
            if (activeStates.get(i).fragment.hasBack())
                if (activeStates.get(i).fragment.back())
                    return true;
        }
        while (backstack.size() != 0) {
            FragmentState.Mode mode = activeStates.get(activeStates.size() - 1).mode;
            FragmentTransaction transaction = backstack.remove(backstack.size() - 1);
            transaction.undo();
            if (mode != FragmentState.Mode.Join)
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
        getContainer(state).addView(view);
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
        getContainer(state).addView(fragment.getView(), 0);
        if (!state.fragmentState.isEmpty())
            fragment.onRestoreState(state.fragmentState);
        if (results.containsKey(fragment.getId()))
            fragment.onResult(results.get(fragment.getId()));
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
        activeStates.remove(state);
        fragment.animateOutAdd(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fragment.pause();
                fragment.getView().setVisibility(View.INVISIBLE);
                fragment.onSaveState(state.fragmentState);
                fragment.getView().setAnimation(null);
                getContainer(state).removeView(fragment.getView());
                state.fragment = null;
            }
        });
    }

    void outBackStateAnimate(final FragmentState state) {
        final Fragment fragment = state.fragment;
        activeStates.remove(state);
        fragment.animateOutBack(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fragment.finish();
                fragment.getView().setVisibility(View.INVISIBLE);
                fragment.getView().setAnimation(null);
                getContainer(state).removeView(fragment.getView());
                state.fragment = null;
            }
        });
    }

    public boolean hasBack() {
        for (FragmentState state : activeStates)
            if (state.fragment.getChildFragmentManager().hasBack() || state.mode != FragmentState.Mode.Join)
                return true;

        return false;
    }

    public boolean hasUp() {
        for (FragmentState state : activeStates)
            if (state.fragment.getChildFragmentManager().hasUp() || state.mode == FragmentState.Mode.Push)
                return true;

        return false;
    }

    public void clear() {
        while (!backstack.isEmpty()) {
            FragmentTransaction transaction = backstack.remove(backstack.size() - 1);
            transaction.undo();
        }
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

        bundle.putIntArray("fragmentManagerActiveStates", activeStateIndices);
        bundle.putParcelableArrayList("fragmentManagerStates", stateBundles);
        bundle.putParcelableArrayList("fragmentManagerTransactions", transactionBundles);
    }

    public void restore(Bundle bundle) {
        restoring = true;

        List<FragmentState> allStates = new ArrayList<>();

        clear();
        int[] activeStateIndices = bundle.getIntArray("fragmentManagerActiveStates");
        ArrayList<Bundle> stateBundles = bundle.getParcelableArrayList("fragmentManagerStates");
        ArrayList<Bundle> transactionBundles = bundle.getParcelableArrayList("fragmentManagerTransactions");

        for (Bundle stateBundle : stateBundles) {
            FragmentState state = new FragmentState();
            state.restore(stateBundle);
            allStates.add(state);
        }

        for (int i = 0; i < activeStateIndices.length; i++) {
            FragmentState state = allStates.get(activeStateIndices[i]);

            if (state.fragment == null)
                state.fragment = instantiate(state.fragmentClass);
            Fragment fragment = state.fragment;
            activeStates.add(state);
            getContainer(state).addView(fragment.getView());
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
    private ViewGroup getContainer(FragmentState transaction) {
        View root = activity.getWindow().getDecorView().getRootView();
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

    public FragmentManager getParent() {
        return parent;
    }

    public void setResult(int id, Bundle result) {
        results.put(id, result);
    }
}
