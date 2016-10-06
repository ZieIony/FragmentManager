package pl.zielony.fragmentmanager;

import pl.zielony.animator.AnimatorListenerAdapter;

/**
 * Created by Marcin on 2016-06-26.
 */
public class LockListenerAdapter extends AnimatorListenerAdapter {
    private FragmentRootView view;

    public LockListenerAdapter(FragmentRootView view) {
        this.view = view;
    }

    @Override
    public void onCancel() {
        view.setLocked(false);
    }

    @Override
    public void onStart() {
        view.setLocked(true);
    }

    @Override
    public void onEnd() {
        view.setLocked(false);
    }
}
