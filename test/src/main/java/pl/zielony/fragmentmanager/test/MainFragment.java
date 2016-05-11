package pl.zielony.fragmentmanager.test;

import android.view.View;

import carbon.widget.Toolbar;
import pl.zielony.fragmentmanager.Fragment;
import pl.zielony.fragmentmanager.FragmentManager;
import pl.zielony.fragmentmanager.FragmentState;

/**
 * Created by Marcin on 2015-12-08.
 */
public class MainFragment extends Fragment {
    public MainFragment(final FragmentManager manager) {
        super(manager);

        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manager.add(Fragment1.class, "container", FragmentState.Mode.Add);
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manager.add(Fragment2.class, "container", FragmentState.Mode.Add);
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
        super.onStart();
        getFragmentManager().add(Fragment1.class, "container", FragmentState.Mode.Join);
    }

    @Override
    protected View onCreateView() {
        return View.inflate(getContext(), R.layout.fragment_main, null);
    }
}
