package pl.zielony.fragmentmanager;

import android.os.Bundle;
import android.util.SparseArray;

/**
 * Created by Marcin on 2016-07-30.
 */

public class StateMachine {
    private static final String STATE = "userState";

    public static final int STATE_NEW = 0;

    private int state = STATE_NEW;

    private SparseArray<SparseArray<EdgeListener>> edges = new SparseArray<>();
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
            throw new IllegalStateException("cannot change userState from " + state + " to userState " + newState);
        setStateInternal(newState);
        update();
    }

    private void setStateInternal(int newState) {
        //Log.e("userState machine", "[" + fragment.getClass().getSimpleName() + ":" + fragment.hashCode() % 100 + "] changed userState from " + userState + " to " + newState);
        EdgeListener listener = edges.get(state).get(newState);
        state = newState;
        listener.onStateChanged();
        if (stateListener != null)
            stateListener.onStateChange(state);
    }

    public void resetState() {
        state = STATE_NEW;
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
