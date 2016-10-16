package pl.zielony.fragmentmanager;

import android.app.Activity;
import android.os.Bundle;

import pl.zielony.statemachine.EdgeListener;
import pl.zielony.statemachine.StateMachine;

/**
 * Created by Marcin on 2016-10-03.
 */

public class FragmentManager extends ManagerBase {
    public FragmentManager(Activity activity, Bundle state) {
        this.activity = activity;
        this.userState = state;
        initStateMachineStates();
    }

    private void initStateMachineStates() {
        StateMachine stateMachine = getStateMachine();
        stateMachine.addEdge(StateMachine.STATE_NEW, STATE_CREATED, new EdgeListener() {
            @Override
            public boolean canChangeState() {
                return desiredState >= STATE_CREATED;
            }

            @Override
            public void onStateChanged() {
                onCreate();
            }
        });
        stateMachine.addEdge(STATE_CREATED, STATE_ATTACHED, new EdgeListener() {
            @Override
            public boolean canChangeState() {
                return desiredState >= STATE_ATTACHED;
            }

            @Override
            public void onStateChanged() {
                onAttach();
            }
        });
        stateMachine.addEdge(STATE_ATTACHED, STATE_STARTED, new EdgeListener() {
            @Override
            public boolean canChangeState() {
                return desiredState >= STATE_STARTED;
            }

            @Override
            public void onStateChanged() {
                onStart();
            }
        });
        stateMachine.addEdge(STATE_STARTED, STATE_RESUMED, new EdgeListener() {
            @Override
            public boolean canChangeState() {
                return desiredState == STATE_RESUMED;
            }

            @Override
            public void onStateChanged() {
                onResume();
            }
        });

        stateMachine.addEdge(STATE_RESUMED, STATE_STARTED, new EdgeListener() {
            @Override
            public boolean canChangeState() {
                return desiredState <= STATE_STARTED;
            }

            @Override
            public void onStateChanged() {
                onPause();
            }
        });
        stateMachine.addEdge(STATE_STARTED, STATE_ATTACHED, new EdgeListener() {
            @Override
            public boolean canChangeState() {
                return desiredState <= STATE_ATTACHED;
            }

            @Override
            public void onStateChanged() {
                onStop();
            }
        });
        stateMachine.addEdge(STATE_ATTACHED, STATE_CREATED, new EdgeListener() {
            @Override
            public boolean canChangeState() {
                return desiredState == STATE_CREATED;
            }

            @Override
            public void onStateChanged() {
                onDetach();
            }
        });
    }

    public void setRootView(FragmentRootView rootView) {
        this.rootView = rootView;
    }
}
