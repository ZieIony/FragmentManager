package pl.zielony.fragmentmanager;

/**
 * Created by Marcin on 2016-08-01.
 */
public interface EdgeListener {
    boolean canChangeState();
    void onStateChanged();
}
