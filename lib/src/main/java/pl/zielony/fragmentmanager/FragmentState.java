package pl.zielony.fragmentmanager;

import android.os.Bundle;

/**
 * Created by Marcin on 2015-12-02.
 */
public class FragmentState {
    public enum Mode {
        Push, Add, Join
    }

    private static final String CLASS = "class", ID = "id", TAG = "tag", MODE = "mode", FRAGMENT = "fragment";

    public Class<? extends Fragment> fragmentClass;
    public Fragment fragment;
    public int id;
    public String tag;
    public Mode mode;
    public Bundle fragmentState;

    public FragmentState() {
        fragmentState = new Bundle();
    }

    public FragmentState(Fragment fragment, int id, String tag, Mode mode) {
        this.fragment = fragment;
        this.fragmentClass = fragment.getClass();
        this.id = id;
        this.tag = tag;
        this.mode = mode;
        fragmentState = new Bundle();
    }

    public void save(Bundle bundle) {
        bundle.putString(CLASS, fragmentClass.getName());
        bundle.putInt(ID, id);
        bundle.putString(TAG, tag);
        bundle.putInt(MODE, mode.ordinal());
        bundle.putBundle(FRAGMENT, fragmentState);
    }

    public void restore(Bundle bundle) {
        try {
            fragmentClass = (Class<? extends Fragment>) Class.forName(bundle.getString(CLASS));
            id = bundle.getInt(ID);
            tag = bundle.getString(TAG);
            mode = Mode.values()[bundle.getInt(MODE)];
            fragmentState = bundle.getBundle(FRAGMENT);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
