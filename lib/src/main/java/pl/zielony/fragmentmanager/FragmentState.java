package pl.zielony.fragmentmanager;

import android.os.Bundle;

/**
 * Created by Marcin on 2015-12-02.
 */
public class FragmentState {

    private static final String CLASS = "class", ID = "layoutId", TAG = "tag", MODE = "mode", FRAGMENT = "fragment";

    public Class<? extends Fragment> fragmentClass;
    public Fragment fragment;
    public int layoutId;
    public String tag;
    public Bundle fragmentState;

    public FragmentState() {
        fragmentState = new Bundle();
    }

    public FragmentState(Fragment fragment, int layoutId, String tag) {
        this.fragment = fragment;
        this.fragmentClass = fragment.getClass();
        this.layoutId = layoutId;
        this.tag = tag;
        fragmentState = new Bundle();
    }

    public void save(Bundle bundle) {
        bundle.putString(CLASS, fragmentClass.getName());
        bundle.putInt(ID, layoutId);
        bundle.putString(TAG, tag);
        bundle.putBundle(FRAGMENT, fragmentState);
    }

    public void restore(Bundle bundle) {
        try {
            fragmentClass = (Class<? extends Fragment>) Class.forName(bundle.getString(CLASS));
            layoutId = bundle.getInt(ID);
            tag = bundle.getString(TAG);
            fragmentState = bundle.getBundle(FRAGMENT);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
