package pl.zielony.fragmentmanager;

public class StateChange {
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
