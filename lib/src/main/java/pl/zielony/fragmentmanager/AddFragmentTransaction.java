package pl.zielony.fragmentmanager;

/**
 * Created by Marcin on 2015-12-31.
 */
public class AddFragmentTransaction implements FragmentTransaction {
    protected final FragmentState state;
    protected final FragmentManager manager;

    public AddFragmentTransaction(FragmentState state, FragmentManager manager) {
        this.state = state;
        this.manager = manager;
    }

    @Override
    public void execute() {
        manager.inAddState(state);
    }

    @Override
    public void undo() {
        manager.outBackState(state);
    }
}
