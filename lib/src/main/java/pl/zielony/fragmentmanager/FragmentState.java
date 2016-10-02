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
    private Bundle state;

    FragmentState() {
        state = new Bundle();
    }

    FragmentState(Fragment fragment, int layoutId, String tag) {
        this.fragment = fragment;
        this.fragmentClass = fragment.getClass();
        this.layoutId = layoutId;
        this.tag = tag;
        state = new Bundle();
    }

    void save(Bundle bundle) {
        bundle.putString(CLASS, fragmentClass.getName());
        bundle.putInt(ID, layoutId);
        bundle.putString(TAG, tag);
        bundle.putBundle(FRAGMENT, state);
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

    public void instantiateFragment(Context context) {
        fragment = Fragment.instantiate(fragmentClass, context);
    }

    public Bundle getState() {
        return state;
    }

    public void clearFragment() {
        fragment = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FragmentState that = (FragmentState) o;

        if (layoutId != that.layoutId) return false;
        if (fragmentClass != null ? !fragmentClass.equals(that.fragmentClass) : that.fragmentClass != null)
            return false;
        return tag != null ? tag.equals(that.tag) : that.tag == null;

    }

}
