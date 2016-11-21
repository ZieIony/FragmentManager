package pl.zielony.fragmentmanager.test;

import android.os.Bundle;

import pl.zielony.fragmentmanager.FragmentActivity;
import pl.zielony.fragmentmanager.FragmentRoute;
import pl.zielony.fragmentmanager.TransactionMode;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null)
            getFragmentManager2().add(MainFragment.class, R.id.root, TransactionMode.Join);

        enableFragmentDebugging();
    }

    @Override
    protected boolean onNavigate(FragmentRoute route) {
        FragmentRoute.Step step = route.removeStep();
        getFragmentManager2().replace(step.getFragment(), R.id.root, step.getMode());
        return true;
    }

}
