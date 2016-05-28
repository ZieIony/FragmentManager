package pl.zielony.fragmentmanager.test;

import android.view.View;

import pl.zielony.fragmentmanager.Fragment;
import pl.zielony.fragmentmanager.FragmentManager;

/**
 * Created by Marcin on 2015-12-08.
 */
public class Fragment2 extends Fragment {
    public Fragment2(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    protected int getViewResId() {
        return R.layout.fragment2;
    }
}
