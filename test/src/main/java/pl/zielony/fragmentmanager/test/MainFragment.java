package pl.zielony.fragmentmanager.test;

import android.view.View;

import carbon.widget.Toolbar;
import pl.zielony.fragmentmanager.Fragment;
import pl.zielony.fragmentmanager.TransactionMode;
import pl.zielony.fragmentmanager.FragmentAnnotation;

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
                getChildFragmentManager().replace(Fragment1.class, "container", TransactionMode.Push).execute();
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getChildFragmentManager().replace(Fragment2.class, "container", TransactionMode.Push).execute();
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
    protected void onStart(int detail) {
        super.onStart(detail);
        if ((detail & Fragment.ADD) != 0)
            getChildFragmentManager().add(Fragment1.class, "container", TransactionMode.Join).execute();
    }
}
