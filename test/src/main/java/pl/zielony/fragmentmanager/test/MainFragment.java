package pl.zielony.fragmentmanager.test;

import android.view.View;

import carbon.widget.Toolbar;
import pl.zielony.fragmentmanager.Fragment;
import pl.zielony.fragmentmanager.FragmentManager;
import pl.zielony.fragmentmanager.FragmentTransaction;

/**
 * Created by Marcin on 2015-12-08.
 */
public class MainFragment extends Fragment {
    public MainFragment(final FragmentManager manager) {
        super(manager);

        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manager.add(Fragment1.class, "container", FragmentTransaction.Mode.Add);
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manager.add(Fragment2.class, "container", FragmentTransaction.Mode.Add);
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.getIconView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (manager.hasUp()) {
                    manager.up();
                } else {
                    getActivity().onBackPressed();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        getFragmentManager().add(Fragment1.class, "container", FragmentTransaction.Mode.Join);
    }

    @Override
    protected int getViewResId() {
        return R.layout.fragment_main;
    }
}
