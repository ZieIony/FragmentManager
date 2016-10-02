package pl.zielony.fragmentmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
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

    static Handler handler = new Handler(Looper.getMainLooper());

    private final Activity activity;
    List<FragmentTransaction> backstack = new ArrayList<>();
    List<FragmentState> activeStates = new ArrayList<>();
    private FragmentRootView root;
    private boolean started = false;
    private boolean resumed = false;

    public FragmentManager(Activity activity) {
        this.activity = activity;
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

    Animator getAddAnimation(final FragmentState state) {
        synchronized (FragmentManager.class) {
            Fragment fragment = state.getFragment();
            FragmentRootView rootView = fragment.getRootView();
            final View view = fragment.getView();
            Animator animator = fragment.animateAdd();
            if (animator != null) {
                //view.setVisibility(View.INVISIBLE);
                animator.addListener(new LockListenerAdapter(rootView));
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        view.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.VISIBLE);
                        animateAddFinished(state);
                        animation.removeListener(this);
                    }
                });
            } else {
                animateAddFinished(state);
            }
            return animator;
        }
    }

    private void animateAddFinished(FragmentState state) {
        synchronized (FragmentManager.class) {
            if (resumed)
                state.getFragment().resume();
        }
    }

    void startState(final FragmentState state) {
        if (state.getFragment() == null)
            state.instantiateFragment(activity);
        Fragment fragment = state.getFragment();
        Log.e("start state", "[" + fragment.getClass().getSimpleName() + ":" + fragment.hashCode() % 100 + "]");
        FragmentRootView rootView = fragment.getRootView();
        if (rootView.getParent() != null || fragment.getFragmentManager() != null)
            throw new IllegalStateException("fragment is already in use");
        fragment.setFragmentManager(this);
        synchronized (FragmentManager.class) {
            activeStates.add(state);
        }
        ViewGroup container = getContainer(state, root);
        container.addView(rootView);
        Bundle fragmentState = state.getState();
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
    }

    Animator getStartAnimation(final FragmentState state) {
        Fragment fragment = state.getFragment();
        FragmentRootView rootView = fragment.getRootView();
        final View view = fragment.getView();
        ViewHelper.setAlpha(view, 1);
        Animator animator = fragment.animateStart();
        if (animator != null) {
            //view.setVisibility(View.INVISIBLE);
            animator.addListener(new LockListenerAdapter(rootView));
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    view.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.VISIBLE);
                    resumeState(state);
                    animation.removeListener(this);
                }
            });
        } else {
            resumeState(state);
        }
        return animator;
    }

    private void resumeState(FragmentState state) {
        synchronized (FragmentManager.class) {
            if (resumed)
                state.getFragment().resume();
        }
    }

    Animator getStopAnimation(final FragmentState state) {
        final Fragment fragment = state.getFragment();
        final FragmentRootView rootView = fragment.getRootView();
        final Animator animator = fragment.animateStop();
        if (animator != null) {
            animator.addListener(new LockListenerAdapter(rootView));
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    removeState(state);
                    animation.removeListener(this);
                }
            });
        } else {
            removeState(state);
        }
        return animator;
    }

    Animator getRemoveAnimation(final FragmentState state) {
        final Fragment fragment = state.getFragment();
        final FragmentRootView view = fragment.getRootView();
        Animator animator = fragment.animateRemove();
        if (animator != null) {
            animator.addListener(new LockListenerAdapter(view));
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    removeState(state);
                    animation.removeListener(this);
                }
            });
        } else {
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
        fragment.onSaveState(state.getState());
        if (fragment.isResumed())
            fragment.pause();
        if (fragment.isStarted())
            fragment.stop();
        if(fragment.isAttached())
            fragment.detach();
        if(fragment.isCreated())
            fragment.destroy();
        synchronized (FragmentManager.class) {
            activeStates.remove(state);
        }
        state.clearFragment();
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
            removeState(state);
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
            startState(state);
            resumeState(state);
        }

        ArrayList<Bundle> transactionBundles = bundle.getParcelableArrayList(TRANSACTIONS);
        for (Bundle transactionBundle : transactionBundles) {
            FragmentTransaction transaction = new FragmentTransaction(this);
            transaction.restore(transactionBundle, allStates);
            backstack.add(transaction);
        }
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
                state.getFragment().getChildFragmentManager().navigate(route);
                return;
            }
        }
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

    public void onDetach(){
        synchronized (FragmentManager.class) {
            List<FragmentState> copy = new ArrayList<>(activeStates);
            for (FragmentState state : copy)
                state.getFragment().detach();
        }
    }

    public void onDestroy(){
        synchronized (FragmentManager.class) {
            List<FragmentState> copy = new ArrayList<>(activeStates);
            for (FragmentState state : copy)
                state.getFragment().destroy();
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
