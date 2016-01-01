package pl.zielony.fragmentmanager;

/**
 * Created by Marcin on 2015-12-31.
 */
public class RemoveFragmentTransaction implements FragmentTransaction {
    protected final FragmentState state;
    protected final FragmentManager manager;

    public RemoveFragmentTransaction(FragmentState state, FragmentManager manager) {
        this.state = state;
        this.manager = manager;
    }

    @Override
    public void execute() {
        manager.outAddState(state);
    }

    @Override
    public void undo() {
        manager.inBackState(state);
    }
}
