package pl.zielony.fragmentmanager;

import android.app.Activity;
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
    private Bundle state;

    FragmentState() {

    }

    FragmentState(Fragment fragment, int layoutId, String tag) {
        this.fragment = fragment;
        this.fragmentClass = fragment.getClass();
        this.layoutId = layoutId;
        this.tag = tag;
    }

    Bundle save() {
        Bundle bundle = new Bundle();
        bundle.putString(CLASS, fragmentClass.getName());
        bundle.putInt(ID, layoutId);
        bundle.putString(TAG, tag);
        if (fragment != null) {
            state = new Bundle();
            fragment.save(state);
        }
        bundle.putBundle(FRAGMENT, state);
        return bundle;
    }

    void restore(Bundle bundle) {
        try {
            fragmentClass = (Class<? extends Fragment>) Class.forName(bundle.getString(CLASS));
            layoutId = bundle.getInt(ID);
            tag = bundle.getString(TAG);
            state = bundle.getBundle(FRAGMENT);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Fragment getFragment() {
        return fragment;
    }

    public void instantiateFragment(Activity activity) {
        fragment = Fragment.instantiate(fragmentClass, activity, state);
    }

    public Bundle getState() {
        return state;
    }

    public void clearFragment() {
        fragment = null;
    }

}
