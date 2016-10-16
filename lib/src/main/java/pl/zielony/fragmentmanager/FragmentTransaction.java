package pl.zielony.fragmentmanager;

import android.os.Bundle;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import pl.zielony.animator.Animator;
import pl.zielony.animator.AnimatorSet;
import pl.zielony.statemachine.OnStateChangeListener;

/**
 * Created by Marcin on 2015-12-31.
 */
public class FragmentTransaction {
    private static final String CHANGES = "changes";
    private static final String STATES = "states";
    private static final String MODE = "mode";
    private static final String SHARED_ELEMENTS = "sharedElements";
    private static final String SHARED_ELEMENT_CLASS = "sharedElementClass";

    private List<StateChange> changes = new ArrayList<>();
    private List<SharedElement> sharedElements = new ArrayList<>();
    private ManagerBase manager;
    private TransactionMode mode;

    public FragmentTransaction(ManagerBase manager) {
        this.manager = manager;
    }

    public FragmentTransaction(ManagerBase manager, TransactionMode mode) {
        this.manager = manager;
        this.mode = mode;
    }

    public TransactionMode getMode() {
        return mode;
    }

    public void addStateChange(FragmentState state, StateChange.Change change) {
        changes.add(new StateChange(state, change));
    }

    public void addSharedElement(SharedElement sharedElement) {
        sharedElements.add(sharedElement);
    }

    public void execute() {
        List<Animator> animators = new ArrayList<>();
        List<Fragment> fragments = new ArrayList<>(manager.getFragments());

        if (mode == TransactionMode.Join && !manager.backstack.isEmpty())
            removeJoinedFragments();

        manager.backstack.add(this);
        for (StateChange stateChange : changes) {
            if (stateChange.getChange() == StateChange.Change.Add) {
                manager.startState(stateChange.getState(), stateChange.getChange());
                Animator animator = manager.prepareAddAnimation(stateChange.getState(), stateChange.getState().getFragment().animateAdd());
                if (animator != null)
                    animators.add(animator);
            } else {
                Animator animator = manager.prepareRemoveAnimation(stateChange.getState(), stateChange.getState().getFragment().animateStop());
                if (animator != null)
                    animators.add(animator);
            }
        }

        fragments.addAll(manager.getFragments());

        runAnimations(animators, fragments, false);
    }

    private void removeJoinedFragments() {
        List<FragmentTransaction> backstack = manager.backstack;

        for (StateChange newChange : changes) {
            FragmentState newState = newChange.getState();

            FragmentTransaction transaction = backstack.get(backstack.size() - 1);
            List<StateChange> changes = transaction.changes;

            for (int i = changes.size() - 1; i >= 0; i--) {
                if (newState == changes.get(i).getState())
                    changes.remove(i);
            }

            if (changes.isEmpty()) {
                backstack.remove(backstack.size() - 1);
                if(backstack.isEmpty())
                    return;
            }
        }
    }

    public void undo() {
        final List<Animator> animators = new ArrayList<>();
        List<Fragment> fragments = new ArrayList<>(manager.getFragments());

        for (int i = changes.size() - 1; i >= 0; i--) {
            StateChange stateChange = changes.get(i);
            if (stateChange.getChange() == StateChange.Change.Add) {
                Animator animator = manager.prepareRemoveAnimation(stateChange.getState(), stateChange.getState().getFragment().animateRemove());
                if (animator != null)
                    animators.add(animator);
            } else {
                manager.startState(stateChange.getState(), stateChange.getChange());
                Animator animator = manager.prepareAddAnimation(stateChange.getState(), stateChange.getState().getFragment().animateStart());
                if (animator != null)
                    animators.add(animator);
            }
        }

        fragments.addAll(manager.getFragments());

        runAnimations(animators, fragments, true);
    }

    private void runAnimations(final List<Animator> animators, final List<Fragment> fragments, final boolean reverse) {
        final AtomicInteger notAttachedFragments = new AtomicInteger();

        notAttachedFragments.set(manager.getFragments().size());

        for (final Fragment f : manager.getFragments()) {
            if (f.isAttached()) {
                f.setOnStateChangeListener(null);
                synchronized (FragmentTransaction.this) {
                    if (notAttachedFragments.decrementAndGet() == 0)
                        FragmentTransaction.this.notify();
                }
            } else {
                f.setOnStateChangeListener(new OnStateChangeListener() {
                    @Override
                    public void onStateChange(int state) {
                        if (f.isAttached()) {
                            f.setOnStateChangeListener(null);
                            synchronized (FragmentTransaction.this) {
                                if (notAttachedFragments.decrementAndGet() == 0)
                                    FragmentTransaction.this.notify();
                            }
                        }
                    }
                });
            }
        }

        new Thread() {
            public void run() {
                try {
                    synchronized (FragmentTransaction.this) {
                        if (notAttachedFragments.get() > 0)
                            FragmentTransaction.this.wait();

                        Handler handler = FragmentManager.getHandler();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                for (SharedElement e : sharedElements)
                                    animators.add(e.start(fragments, reverse, manager.getRootView()));
                                AnimatorSet set = new AnimatorSet();
                                set.addAll(animators);
                                set.start();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        }.start();
    }

    void save(Bundle bundle, List<FragmentState> allStates) {
        int[] changes = new int[this.changes.size()];
        int[] states = new int[this.changes.size()];
        for (int i = 0; i < this.changes.size(); i++) {
            StateChange change = this.changes.get(i);
            changes[i] = change.getChange().ordinal();
            if (!allStates.contains(change.getState()))
                allStates.add(change.getState());
            states[i] = allStates.indexOf(change.getState());
        }

        ArrayList<Bundle> sharedElementBundles = new ArrayList<>();
        for (SharedElement sharedElement : sharedElements) {
            Bundle sharedElementBundle = new Bundle();
            sharedElementBundle.putString(SHARED_ELEMENT_CLASS, sharedElement.getClass().getName());
            sharedElement.save(sharedElementBundle);
            sharedElementBundles.add(sharedElementBundle);
        }

        bundle.putIntArray(CHANGES, changes);
        bundle.putIntArray(STATES, states);
        bundle.putInt(MODE, mode.ordinal());
        bundle.putParcelableArrayList(SHARED_ELEMENTS, sharedElementBundles);
    }

    void restore(Bundle bundle, List<FragmentState> allStates) {
        int[] changes = bundle.getIntArray(CHANGES);
        int[] states = bundle.getIntArray(STATES);
        mode = TransactionMode.values()[bundle.getInt(MODE)];
        ArrayList<Bundle> sharedElementBundles = bundle.getParcelableArrayList(SHARED_ELEMENTS);

        if (changes == null || states == null || mode == null || sharedElementBundles == null)
            throw new IllegalStateException("Cannot restore transaction, because some of restore data is missing");

        for (int i = 0; i < changes.length; i++)
            this.changes.add(new StateChange(allStates.get(states[i]), StateChange.Change.values()[changes[i]]));

        for (Bundle sharedElementBundle : sharedElementBundles) {
            String className = sharedElementBundle.getString(SHARED_ELEMENT_CLASS);
            SharedElement sharedElement = null;
            try {
                sharedElement = (SharedElement) Class.forName(className).newInstance();
                sharedElement.restore(sharedElementBundle);
                sharedElements.add(sharedElement);
            } catch (Exception e) {
            }
        }
    }


    // -------------------
    // add
    // -------------------

    public <T extends Fragment> void add(T fragment, int id) {
        addStateChange(new FragmentState(fragment, id, null), StateChange.Change.Add);
    }

    public <T extends Fragment> void add(T fragment, String tag) {
        addStateChange(new FragmentState(fragment, 0, tag), StateChange.Change.Add);
    }

    public <T extends Fragment> void add(Class<T> fragmentClass, int id) {
        T fragment = Fragment.instantiate(fragmentClass, manager.getActivity(), null);
        addStateChange(new FragmentState(fragment, id, null), StateChange.Change.Add);
    }

    public <T extends Fragment> void add(Class<T> fragmentClass, String tag) {
        T fragment = Fragment.instantiate(fragmentClass, manager.getActivity(), null);
        addStateChange(new FragmentState(fragment, 0, tag), StateChange.Change.Add);
    }


    // -------------------
    // replace
    // -------------------

    public <T extends Fragment> void replace(T fragment, int id) {
        replaceFragment(null, fragment, id, null);
    }

    public <T extends Fragment> void replace(T fragment, String tag) {
        replaceFragment(null, fragment, 0, tag);
    }

    public <T extends Fragment, T2 extends Fragment> void replace(T2 removeFragment, T fragment) {
        replaceFragment(removeFragment, fragment, 0, null);
    }

    public <T extends Fragment> void replace(Class<T> fragmentClass, int id) {
        T fragment = Fragment.instantiate(fragmentClass, manager.getActivity(), null);
        replaceFragment(null, fragment, id, null);
    }

    public <T extends Fragment> void replace(Class<T> fragmentClass, String tag) {
        T fragment = Fragment.instantiate(fragmentClass, manager.getActivity(), null);
        replaceFragment(null, fragment, 0, tag);
    }

    public <T extends Fragment, T2 extends Fragment> void replace(T2 removeFragment, Class<T> fragmentClass) {
        T fragment = Fragment.instantiate(fragmentClass, manager.getActivity(), null);
        replaceFragment(removeFragment, fragment, 0, null);
    }

    private void replaceFragment(Fragment removeFragment, Fragment fragment, int id, String tag) {
        for (FragmentState state : manager.activeStates) {
            if (state.getFragment() == removeFragment || state.layoutId == id || tag != null && tag.equals(state.tag)) {
                addStateChange(state, StateChange.Change.Remove);
                addStateChange(new FragmentState(fragment, id, tag), StateChange.Change.Add);
                return;
            }
        }
    }


    // -------------------
    // remove
    // -------------------

    public void remove(int id) {
        removeFragment(null, id, null);
    }

    public void remove(String tag) {
        removeFragment(null, 0, tag);
    }

    public <T extends Fragment> void remove(T fragment) {
        removeFragment(fragment, 0, null);
    }

    private void removeFragment(Fragment removeFragment, int id, String tag) {
        for (FragmentState state : manager.activeStates) {
            if (state.getFragment() == removeFragment || state.layoutId == id || tag != null && tag.equals(state.tag)) {
                addStateChange(state, StateChange.Change.Remove);
                break;
            }
        }
    }
}
