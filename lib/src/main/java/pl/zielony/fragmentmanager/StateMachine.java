package pl.zielony.fragmentmanager;

import android.os.Bundle;
import android.util.SparseArray;

/**
 * Created by Marcin on 2016-07-30.
 */

public class StateMachine {
    private static final String STATE = "state";

    public static final int STATE_NEW = 0;

    private int state = STATE_NEW;

    private SparseArray<SparseArray<EdgeListener>> edges = new SparseArray<>();
    //private List<QueuedState> queuedStates = new ArrayList<>();
    private OnStateChangeListener stateListener;

    public void save(Bundle bundle) {
        bundle.putInt(STATE, state);
    }

    public void restore(Bundle bundle) {
        state = bundle.getInt(STATE);
    }

   // public FragmentManager fragment;

    public void setState(int newState) {
        if (!hasEdge(state, newState))
            throw new IllegalStateException("cannot change state from " + state + " to state " + newState);
        setStateInternal(newState);
        update();
    }

    private void setStateInternal(int newState) {
        //Log.e("state machine", "[" + fragment.getClass().getSimpleName() + ":" + fragment.hashCode() % 100 + "] changed state from " + state + " to " + newState);
        EdgeListener listener = edges.get(state).get(newState);
        state = newState;
        listener.onStateChanged();
        if (stateListener != null)
            stateListener.onStateChange(state);

        /*if (queuedStates.isEmpty())
            return;

        QueuedState queuedState = queuedStates.get(0);
        if (hasEdge(state, queuedState.state)) {
            queuedStates.remove(0);
            setState(queuedState.state, queuedState.param);
        }*/
    }

    public void resetState() {
        state = STATE_NEW;
        //queuedStates.clear();
    }

    public void addEdge(int stateFrom, int stateTo, EdgeListener listener) {
        if (edges.indexOfKey(stateFrom) < 0) {
            SparseArray<EdgeListener> list = new SparseArray<>();
            list.put(stateTo, listener);
            edges.put(stateFrom, list);
        } else {
            edges.get(stateFrom).put(stateTo, listener);
        }
    }

    public boolean hasEdge(int stateFrom, int stateTo) {
        return edges.indexOfKey(stateFrom) >= 0 && edges.get(stateFrom).indexOfKey(stateTo) >= 0;
    }

    public int getState() {
        return state;
    }

    /*public <Type> void queueState(int newState, Type param) {
        if (queuedStates.isEmpty() && hasEdge(state, newState)) {
            setState(newState, param);
            return;
        }
        queuedStates.add(new QueuedState(newState, param));
    }

    private static class QueuedState {
        private final int state;
        private final Object param;

        public <Type> QueuedState(int state, Type param) {
            this.state = state;
            this.param = param;
        }
    }*/

    public void setOnStateChangeListener(OnStateChangeListener stateListener) {
        this.stateListener = stateListener;
    }

    public void update() {
        while (updateInternal()) ;
    }

    private boolean updateInternal() {
        SparseArray<EdgeListener> listeners = edges.get(state);
        for (int i = 0; i < listeners.size(); i++) {
            int newState = listeners.keyAt(i);
            if (listeners.get(newState).canChangeState()) {
                setStateInternal(newState);
                return true;
            }
        }
        return false;
    }
}
