package pl.zielony.fragmentmanager.test;

import android.view.View;

import carbon.widget.Toolbar;
import pl.zielony.fragmentmanager.Fragment;
import pl.zielony.fragmentmanager.FragmentAnnotation;
import pl.zielony.fragmentmanager.TransactionMode;

/**
 * Created by Marcin on 2015-12-08.
 */
@FragmentAnnotation(layout = R.layout.fragment_main)
public class MainFragment extends Fragment {
    @Override
    protected void onCreate() {
        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replace(RedFragment.class, "container", TransactionMode.Push);
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replace(PerryListFragment.class, "container", TransactionMode.Push);
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.getIconView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getFragmentManager().hasUp()) {
                    getFragmentManager().upTraverse();
                } else {
                    getActivity().onBackPressed();
                }
            }
        });
    }

    @Override
    protected void onStart(boolean fresh) {
        super.onStart(fresh);
        if (fresh) {
            add(RedFragment.class, "container", TransactionMode.Join);
            add(DrawerFragment.class, R.id.drawer, TransactionMode.Join);
        }
    }
}
