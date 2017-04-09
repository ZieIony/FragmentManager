package pl.zielony.fragmentmanager;

import pl.zielony.animator.AnimatorListener;

class LockListenerAdapter implements AnimatorListener {
    private FragmentRootView rootView;

    LockListenerAdapter(FragmentRootView rootView) {
        this.rootView = rootView;
    }

    @Override
    public void onStart() {
        rootView.enterAnimationMode();
    }

    @Override
    public void onEnd() {
        rootView.leaveAnimationMode();
    }

    @Override
    public void onCancel() {
        rootView.leaveAnimationMode();
    }
}
