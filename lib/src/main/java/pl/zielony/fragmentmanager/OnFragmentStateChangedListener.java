package pl.zielony.fragmentmanager;

/**
 * Created by Marcin on 2017-04-09.
 */

public interface OnFragmentStateChangedListener {
    void onAttachedChanged(boolean attached);

    void onStartedChanged(boolean started);

    void onCreateChanged(boolean created);

    void onResumedChanged(boolean resumed);
}
