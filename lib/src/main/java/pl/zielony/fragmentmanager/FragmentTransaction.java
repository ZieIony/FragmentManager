package pl.zielony.fragmentmanager;

import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marcin on 2015-12-31.
 */
public class FragmentTransaction {
    List<StateChange> changes = new ArrayList<>();
    List<SharedElement> sharedElements = new ArrayList<>();
    private FragmentManager manager;
    private Mode mode;

    public enum Mode {
        Push, Add, Join
    }

    public static class StateChange {
        public enum Change {
            Add, Remove
        }


        private final FragmentState state;
        private final Change change;

        public StateChange(FragmentState state, Change change) {
            this.state = state;
            this.change = change;
        }

        public Change getChange() {
            return change;
        }

        public FragmentState getState() {
            return state;
        }
    }

    public FragmentTransaction(FragmentManager fragmentManager) {
        this.manager = fragmentManager;
    }

    public FragmentTransaction(FragmentManager manager, Mode mode) {
        this.manager = manager;
        this.mode = mode;
    }

    public Mode getMode() {
        return mode;
    }

    public void addStateChange(FragmentState state, StateChange.Change change) {
        changes.add(new StateChange(state, change));
    }

    public void addSharedElement(View view, Fragment from, Fragment to) {
        sharedElements.add(new SharedElement(view, from, to));
    }

    public void addSharedElement(int id, Fragment from, Fragment to) {
        sharedElements.add(new SharedElement(id, from, to));
    }

    public void execute() {
        List<Fragment> prevFragments = new ArrayList<>(manager.getFragments());
        manager.backstack.add(this);
        for (StateChange stateChange : changes) {
            if (stateChange.change == StateChange.Change.Add) {
                manager.inAddState(stateChange.state);
            } else {
                // manager.outAddState(stateChange.state);
            }
        }
        for (StateChange stateChange : changes) {
            if (stateChange.change == StateChange.Change.Add) {
                manager.inAddStateAnimate(stateChange.state);
            } else {
                manager.outAddStateAnimate(stateChange.state);
            }
        }
        prevFragments.addAll(manager.getFragments());
        for (SharedElement e : sharedElements)
            e.apply(prevFragments, false);
    }

    public void undo() {
        List<Fragment> prevFragments = new ArrayList<>(manager.getFragments());
        for (int i = changes.size() - 1; i >= 0; i--) {
            StateChange stateChange = changes.get(i);
            if (stateChange.change == StateChange.Change.Add) {
                // manager.outBackState(stateChange.state);
            } else {
                manager.inBackState(stateChange.state);
            }
        }
        for (int i = changes.size() - 1; i >= 0; i--) {
            StateChange stateChange = changes.get(i);
            if (stateChange.change == StateChange.Change.Add) {
                manager.outBackStateAnimate(stateChange.state);
            } else {
                manager.inBackStateAnimate(stateChange.state);
            }
        }
        prevFragments.addAll(manager.getFragments());
        for (SharedElement e : sharedElements)
            e.apply(prevFragments, true);
    }

    public void save(Bundle bundle, List<FragmentState> allStates) {
        int[] modes = new int[changes.size()];
        int[] states = new int[changes.size()];
        for (int i = 0; i < changes.size(); i++) {
            StateChange change = changes.get(i);
            modes[i] = change.change.ordinal();
            if (!allStates.contains(change.state))
                allStates.add(change.state);
            states[i] = allStates.indexOf(change.state);
        }
        bundle.putIntArray("modes", modes);
        bundle.putIntArray("states", states);
        bundle.putInt("mode", mode.ordinal());
    }

    public void restore(Bundle bundle, List<FragmentState> allStates) {
        int[] modes = bundle.getIntArray("modes");
        int[] states = bundle.getIntArray("states");
        mode = Mode.values()[bundle.getInt("mode")];

        for (int i = 0; i < modes.length; i++)
            changes.add(new StateChange(allStates.get(states[i]), StateChange.Change.values()[modes[i]]));
    }
}
