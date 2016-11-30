package pl.zielony.fragmentmanager;

import android.app.Activity;
import android.os.Bundle;

import pl.zielony.statemachine.StateMachine;

/**
 * Created by Marcin on 2016-10-03.
 */

public class FragmentManager extends ManagerBase {
    public FragmentManager(Activity activity, Bundle state) {
        this.activity = activity;
        this.userState = state;

        StateMachine stateMachine = getStateMachine();
        stateMachine.addEdge(StateMachine.STATE_NEW, STATE_CREATED, () -> desiredState >= STATE_CREATED, this::onCreate);
        stateMachine.addEdge(STATE_CREATED, STATE_ATTACHED, () -> desiredState >= STATE_ATTACHED, this::onAttach);
        stateMachine.addEdge(STATE_ATTACHED, STATE_STARTED, () -> desiredState >= STATE_STARTED, this::onStart);
        stateMachine.addEdge(STATE_STARTED, STATE_RESUMED, () -> desiredState == STATE_RESUMED, this::onResume);
        stateMachine.addEdge(STATE_RESUMED, STATE_STARTED, () -> desiredState <= STATE_STARTED, this::onPause);
        stateMachine.addEdge(STATE_STARTED, STATE_ATTACHED, () -> desiredState <= STATE_ATTACHED, this::onStop);
        stateMachine.addEdge(STATE_ATTACHED, STATE_CREATED, () -> desiredState == STATE_CREATED, this::onDetach);
    }

    public void setRootView(FragmentRootView rootView) {
        this.rootView = rootView;
    }
}
