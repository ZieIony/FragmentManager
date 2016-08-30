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
@FragmentAnnotation(layout = R.layout.fragment2, pooling = false)
public class Fragment2 extends Fragment {
    @Override
    protected void onCreate() {

        findViewById(R.id.row).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment3 fragment3 = getFragmentManager().instantiate(Fragment3.class);
                FragmentTransaction transaction = getFragmentManager().replace(fragment3, "container", TransactionMode.Push);
                transaction.addSharedElement(new ViewSharedElement(findViewById(R.id.image), Fragment2.this, fragment3));
                transaction.addSharedElement(new TextViewSharedElement((TextView)findViewById(R.id.title), Fragment2.this, fragment3));
                transaction.execute();
            }
        });
    }
}
