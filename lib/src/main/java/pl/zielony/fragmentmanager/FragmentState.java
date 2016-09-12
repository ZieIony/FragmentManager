package pl.zielony.fragmentmanager;

import android.content.Context;
import android.os.Bundle;

/**
 * Created by Marcin on 2015-12-02.
 */
class FragmentState {

    private static final String CLASS = "class", ID = "layoutId", TAG = "tag", FRAGMENT = "fragment";

    private Class<? extends Fragment> fragmentClass;
    private Fragment fragment;
    int layoutId;
    String tag;
    private Bundle fragmentState;

    FragmentState() {
        fragmentState = new Bundle();
    }

    FragmentState(Fragment fragment, int layoutId, String tag) {
        this.fragment = fragment;
        this.fragmentClass = fragment.getClass();
        this.layoutId = layoutId;
        this.tag = tag;
        fragmentState = new Bundle();
    }

    void save(Bundle bundle) {
        bundle.putString(CLASS, fragmentClass.getName());
        bundle.putInt(ID, layoutId);
        bundle.putString(TAG, tag);
        bundle.putBundle(FRAGMENT, fragmentState);
    }

    void restore(Bundle bundle) {
        try {
            fragmentClass = (Class<? extends Fragment>) Class.forName(bundle.getString(CLASS));
            layoutId = bundle.getInt(ID);
            tag = bundle.getString(TAG);
            fragmentState = bundle.getBundle(FRAGMENT);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Fragment getFragment() {
        return fragment;
    }

    public void instantiateFragment(Context context) {
        fragment = Fragment.instantiate(fragmentClass, context);
    }

    public Bundle getFragmentState() {
        return fragmentState;
    }

    public void clearFragment() {
        fragment = null;
    }
}
