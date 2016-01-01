package pl.zielony.fragmentmanager;

/**
 * Created by Marcin on 2015-12-31.
 */
public interface FragmentTransaction {
    void execute();

    void undo();
}
