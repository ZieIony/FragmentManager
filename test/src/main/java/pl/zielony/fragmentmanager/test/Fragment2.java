package pl.zielony.fragmentmanager.test;

import android.view.View;

import pl.zielony.fragmentmanager.Fragment;
import pl.zielony.fragmentmanager.FragmentManager;
import pl.zielony.fragmentmanager.FragmentState;
import pl.zielony.fragmentmanager.FragmentTransaction;
import pl.zielony.fragmentmanager.SharedElement;

/**
 * Created by Marcin on 2015-12-08.
 */
public class Fragment2 extends Fragment {
    public Fragment2(FragmentManager fragmentManager) {
        super(fragmentManager);

        findViewById(R.id.row).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment3 fragment3 = getFragmentManager().instantiate(Fragment3.class);
                FragmentTransaction transaction = getFragmentManager().replace(fragment3, "container", FragmentTransaction.Mode.Push);
                transaction.addSharedElement(new SharedElement(findViewById(R.id.image), Fragment2.this, fragment3));
                transaction.execute();
            }
        });
    }

    @Override
    protected int getViewResId() {
        return R.layout.fragment2;
    }
}
