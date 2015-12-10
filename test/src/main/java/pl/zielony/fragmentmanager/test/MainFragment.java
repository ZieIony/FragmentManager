package pl.zielony.fragmentmanager.test;

import android.content.Context;
import android.view.View;

import carbon.widget.Toolbar;
import pl.zielony.fragmentmanager.Fragment;
import pl.zielony.fragmentmanager.FragmentManager;

/**
 * Created by Marcin on 2015-12-08.
 */
public class MainFragment extends Fragment {
    public MainFragment(final FragmentManager manager) {
        super(manager);

        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manager.add(Fragment1.class,"container");
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manager.add(Fragment2.class,"container");
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.getIconView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(manager.hasUp()) {
                    manager.up();
                }else{
                    getActivity().onBackPressed();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getFragmentManager().join(Fragment1.class,"container");
    }

    @Override
    protected View onCreateView() {
        return View.inflate(getContext(),R.layout.fragment_main,null);
    }
}
