package pl.zielony.fragmentmanager.test;

import android.view.View;

import pl.zielony.fragmentmanager.Fragment;
import pl.zielony.fragmentmanager.FragmentTransaction;
import pl.zielony.fragmentmanager.SharedElement;
import pl.zielony.fragmentmanager.TransactionMode;
import pl.zielony.fragmentmanager.FragmentAnnotation;

/**
 * Created by Marcin on 2015-12-08.
 */
@FragmentAnnotation(layout = R.layout.fragment2)
public class Fragment2 extends Fragment {
    @Override
    protected void onCreate() {

        findViewById(R.id.row).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment3 fragment3 = getFragmentManager().instantiate(Fragment3.class);
                FragmentTransaction transaction = getFragmentManager().replace(fragment3, "container", TransactionMode.Push);
                transaction.addSharedElement(new SharedElement(findViewById(R.id.image), Fragment2.this, fragment3));
                transaction.execute();
            }
        });
    }
}
