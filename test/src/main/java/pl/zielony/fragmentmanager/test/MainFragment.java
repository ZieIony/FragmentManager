package pl.zielony.fragmentmanager.test;

import android.view.View;

import carbon.widget.Toolbar;
import pl.zielony.fragmentmanager.Fragment;
import pl.zielony.fragmentmanager.FragmentManager;
import pl.zielony.fragmentmanager.FragmentTransaction;
import pl.zielony.fragmentmanager.XmlFragment;

/**
 * Created by Marcin on 2015-12-08.
 */
@XmlFragment(layout = R.layout.fragment_main)
public class MainFragment extends Fragment {
    @Override
    protected void onCreate() {
        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().replace(Fragment1.class, "container", FragmentTransaction.Mode.Push).execute();
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().replace(Fragment2.class, "container", FragmentTransaction.Mode.Push).execute();
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.getIconView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getFragmentManager().hasUp()) {
                    getFragmentManager().up();
                } else {
                    getActivity().onBackPressed();
                }
            }
        });

        getFragmentManager().add(Fragment1.class, "container", FragmentTransaction.Mode.Join).execute();
    }
}
