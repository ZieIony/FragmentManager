package pl.zielony.fragmentmanager.test;

import android.support.annotation.NonNull;
import android.view.View;

import pl.zielony.fragmentmanager.Fragment;
import pl.zielony.fragmentmanager.FragmentActivity;
import pl.zielony.fragmentmanager.FragmentAnnotation;
import pl.zielony.fragmentmanager.FragmentRoute;
import pl.zielony.fragmentmanager.TransactionMode;

/**
 * Created by Marcin on 2016-09-27.
 */
@FragmentAnnotation(layout = R.layout.fragment_drawer)
public class DrawerFragment extends Fragment {
    @Override
    protected void onCreate() {
        findViewById(R.id.main).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FragmentActivity)getActivity()).navigate(new FragmentRoute(MainFragment.class, TransactionMode.Add));
            }
        });
    }

    @NonNull
    @Override
    public View getView() {
        return super.getView();
    }
}
