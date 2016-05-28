package pl.zielony.fragmentmanager;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marcin on 2015-12-31.
 */
public class FragmentTransaction {
    List<StateChange> changes = new ArrayList<>();
    private FragmentManager manager;

    public enum Mode {
        Add, Remove
    }

    public static class StateChange {

        private final FragmentState state;
        private final Mode mode;

        public StateChange(FragmentState state, Mode mode) {
            this.state = state;
            this.mode = mode;
        }

        public Mode getMode() {
            return mode;
        }

        public FragmentState getState() {
            return state;
        }
    }

    public FragmentTransaction(FragmentManager manager) {
        this.manager = manager;
    }

    public FragmentTransaction(FragmentManager manager, FragmentState state, Mode mode) {
        this.manager = manager;
        addStateChange(state, mode);
    }

    public void addStateChange(FragmentState state, Mode mode) {
        changes.add(new StateChange(state, mode));
    }

    void execute() {
        manager.backstack.add(this);
        for (StateChange stateChange : changes) {
            if (stateChange.mode == Mode.Add) {
                manager.inAddState(stateChange.state);
            } else {
                // manager.outAddState(stateChange.state);
            }
        }
        for (StateChange stateChange : changes) {
            if (stateChange.mode == Mode.Add) {
                manager.inAddStateAnimate(stateChange.state);
            } else {
                manager.outAddStateAnimate(stateChange.state);
            }
        }
    }

    public void undo() {
        for (int i = changes.size() - 1; i >= 0; i--) {
            StateChange stateChange = changes.get(i);
            if (stateChange.mode == Mode.Add) {
                // manager.outBackState(stateChange.state);
            } else {
                manager.inBackState(stateChange.state);
            }
        }
        for (int i = changes.size() - 1; i >= 0; i--) {
            StateChange stateChange = changes.get(i);
            if (stateChange.mode == Mode.Add) {
                manager.outBackStateAnimate(stateChange.state);
            } else {
                manager.inBackStateAnimate(stateChange.state);
            }
        }
    }

    public void save(Bundle bundle, List<FragmentState> allStates) {
        int[] modes = new int[changes.size()];
        int[] states = new int[changes.size()];
        for (int i = 0; i < changes.size(); i++) {
            StateChange change = changes.get(i);
            modes[i] = change.mode.ordinal();
            if (!allStates.contains(change.state))
                allStates.add(change.state);
            states[i] = allStates.indexOf(change.state);
        }
        bundle.putIntArray("modes", modes);
        bundle.putIntArray("states", states);
    }

    public void restore(Bundle bundle, List<FragmentState> allStates) {
        int[] modes = bundle.getIntArray("modes");
        int[] states = bundle.getIntArray("states");

        for (int i = 0; i < modes.length; i++)
            changes.add(new StateChange(allStates.get(states[i]), Mode.values()[modes[i]]));
    }
}
