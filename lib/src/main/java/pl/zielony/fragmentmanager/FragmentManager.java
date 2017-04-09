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
        stateMachine.addEdge(StateMachine.STATE_NEW, STATE_CREATED, () -> desiredState >= STATE_CREATED, __ -> onCreate());
        stateMachine.addEdge(STATE_CREATED, STATE_ATTACHED, () -> desiredState >= STATE_ATTACHED, __ -> onAttach());
        stateMachine.addEdge(STATE_ATTACHED, STATE_STARTED, () -> desiredState >= STATE_STARTED, __ -> onStart());
        stateMachine.addEdge(STATE_STARTED, STATE_RESUMED, () -> desiredState == STATE_RESUMED, __ -> onResume());
        stateMachine.addEdge(STATE_RESUMED, STATE_STARTED, () -> desiredState <= STATE_STARTED, __ -> onPause());
        stateMachine.addEdge(STATE_STARTED, STATE_ATTACHED, () -> desiredState <= STATE_ATTACHED, __ -> onStop());
        stateMachine.addEdge(STATE_ATTACHED, STATE_CREATED, () -> desiredState == STATE_CREATED, __ -> onDetach());
        stateMachine.addEdge(STATE_CREATED, StateMachine.STATE_NEW, () -> desiredState == StateMachine.STATE_NEW, __ -> onDestroy());
    }

    public void setRootView(FragmentRootView rootView) {
        this.rootView = rootView;
    }
}
