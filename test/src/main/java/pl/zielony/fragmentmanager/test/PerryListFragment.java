package pl.zielony.fragmentmanager.test;

import android.view.View;
import android.widget.TextView;

import pl.zielony.fragmentmanager.Fragment;
import pl.zielony.fragmentmanager.FragmentAnnotation;
import pl.zielony.fragmentmanager.FragmentTransaction;
import pl.zielony.fragmentmanager.TextViewSharedElement;
import pl.zielony.fragmentmanager.TransactionMode;
import pl.zielony.fragmentmanager.ViewSharedElement;

/**
 * Created by Marcin on 2015-12-08.
 */
@FragmentAnnotation(layout = R.layout.fragment_perrylist)
public class PerryListFragment extends Fragment {
    @Override
    protected void onCreate() {

        findViewById(R.id.row).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DetailFragment detailFragment = Fragment.instantiate(DetailFragment.class, getContext());
                FragmentTransaction transaction = new FragmentTransaction(getFragmentManager(), TransactionMode.Push);
                transaction.replace(detailFragment, "container");
                transaction.addSharedElement(new ViewSharedElement(findViewById(R.id.image), PerryListFragment.this, detailFragment));
                transaction.addSharedElement(new TextViewSharedElement((TextView) findViewById(R.id.title), PerryListFragment.this, detailFragment));
                transaction.execute();
            }
        });
    }
}
