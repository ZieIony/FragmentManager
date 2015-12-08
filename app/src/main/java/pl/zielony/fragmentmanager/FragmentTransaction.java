package pl.zielony.fragmentmanager;

/**
 * Created by Marcin on 2015-12-02.
 */
public class FragmentTransaction {
    public Class<? extends Fragment> fragmentClass;
    public Fragment fragment;
    public int id;
    public String tag;
    public final boolean backstack;

    public FragmentTransaction(Fragment fragment, int id, String tag, boolean backstack) {
        this.fragment = fragment;
        this.fragmentClass = fragment.getClass();
        this.id = id;
        this.tag = tag;
        this.backstack = backstack;
    }
}
