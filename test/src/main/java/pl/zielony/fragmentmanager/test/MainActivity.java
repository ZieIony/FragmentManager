package pl.zielony.fragmentmanager.test;

import android.os.Bundle;

import pl.zielony.fragmentmanager.FragmentActivity;
import pl.zielony.fragmentmanager.FragmentTreeView;
import pl.zielony.fragmentmanager.TransactionMode;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentTreeView tree = (FragmentTreeView) findViewById(R.id.tree);
        tree.setFragmentManager(getFragmentManager2());

        if (savedInstanceState == null)
            getFragmentManager2().add(MainFragment.class, R.id.root, TransactionMode.Join).execute();
    }

}
