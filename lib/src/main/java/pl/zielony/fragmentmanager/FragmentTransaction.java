package pl.zielony.fragmentmanager;

import android.os.Bundle;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Marcin on 2015-12-31.
 */
public class FragmentTransaction {
    private static final String CHANGES = "modes";
    private static final String STATES = "states";
    private static final String MODE = "mode";
    private static final String SHARED_ELEMENTS = "sharedElements";
    private static final String SHARED_ELEMENT_CLASS = "sharedElementClass";

    List<StateChange> changes = new ArrayList<>();
    private List<SharedElement> sharedElements = new ArrayList<>();
    private FragmentManager manager;
    private TransactionMode mode;

    static class StateChange {
        enum Change {
            Add, Remove
        }

        private final FragmentState state;
        private final Change change;

        StateChange(FragmentState state, Change change) {
            this.state = state;
            this.change = change;
        }

        Change getChange() {
            return change;
        }

        FragmentState getState() {
            return state;
        }
    }

    public FragmentTransaction(FragmentManager fragmentManager) {
        this.manager = fragmentManager;
    }

    public FragmentTransaction(FragmentManager manager, TransactionMode mode) {
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

        manager.backstack.add(this);
        for (StateChange stateChange : changes) {
            if (stateChange.change == StateChange.Change.Add) {
                Animator animator = manager.addState(stateChange.state);
                if (animator != null)
                    animators.add(animator);
            } else {
                Animator animator = manager.stopState(stateChange.state);
                if (animator != null)
                    animators.add(animator);
            }
        }

        fragments.addAll(manager.getFragments());

        runAnimations(animators, fragments, false);
    }

    public void undo() {
        final List<Animator> animators = new ArrayList<>();
        List<Fragment> fragments = new ArrayList<>(manager.getFragments());

        for (int i = changes.size() - 1; i >= 0; i--) {
            StateChange stateChange = changes.get(i);
            if (stateChange.change == StateChange.Change.Add) {
                Animator animator = manager.removeState(stateChange.state);
                if (animator != null)
                    animators.add(animator);
            } else {
                Animator animator = manager.startState(stateChange.state);
                if (animator != null)
                    animators.add(animator);
            }
        }

        fragments.addAll(manager.getFragments());

        runAnimations(animators, fragments, true);
    }

    private void runAnimations(final List<Animator> animators, final List<Fragment> fragments, final boolean reverse) {
        final AtomicInteger notAttachedFragments = new AtomicInteger();

        new Thread() {
            public void run() {
                try {
                    synchronized (FragmentTransaction.this) {
                        if (notAttachedFragments.get() > 0)
                            FragmentTransaction.this.wait();

                        fragments.get(0).getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                for (SharedElement e : sharedElements)
                                    animators.add(e.start(fragments, reverse, manager.getRootView()));
                                AnimatorSet set = new AnimatorSet();
                                set.playTogether(animators);
                                set.start();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        }.start();

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
    }

    void save(Bundle bundle, List<FragmentState> allStates) {
        int[] changes = new int[this.changes.size()];
        int[] states = new int[this.changes.size()];
        for (int i = 0; i < this.changes.size(); i++) {
            StateChange change = this.changes.get(i);
            changes[i] = change.change.ordinal();
            if (!allStates.contains(change.state))
                allStates.add(change.state);
            states[i] = allStates.indexOf(change.state);
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
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
