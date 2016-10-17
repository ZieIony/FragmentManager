package pl.zielony.fragmentmanager.test;

import android.view.View;

import pl.zielony.fragmentmanager.Fragment;
import pl.zielony.fragmentmanager.FragmentAnnotation;
import pl.zielony.fragmentmanager.FragmentTransaction;
import pl.zielony.fragmentmanager.ModalFragmentAnimator;
import pl.zielony.fragmentmanager.TextViewSharedElement;
import pl.zielony.fragmentmanager.TransactionMode;
import pl.zielony.fragmentmanager.ViewSharedElement;

/**
 * Created by Marcin on 2015-12-08.
 */
@FragmentAnnotation(layout = R.layout.fragment_perrylist, animator = ModalFragmentAnimator.class)
public class PerryListFragment extends Fragment {
    @Override
    protected void onCreate() {

        findViewById(R.id.row).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DetailFragment detailFragment = Fragment.instantiate(DetailFragment.class, getActivity());
                FragmentTransaction transaction = new FragmentTransaction(getManager(), TransactionMode.Push);
                transaction.replace(detailFragment, "container");
                transaction.addSharedElement(new ViewSharedElement(findViewById(R.id.image), PerryListFragment.this, detailFragment));
                transaction.addSharedElement(new TextViewSharedElement(findViewById(R.id.title), PerryListFragment.this, detailFragment));
                transaction.execute();
            }
        });
    }
}
