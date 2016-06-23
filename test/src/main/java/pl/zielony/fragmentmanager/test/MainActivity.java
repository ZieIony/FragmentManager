package pl.zielony.fragmentmanager.test;

import android.os.Bundle;

import pl.zielony.fragmentmanager.FragmentActivity;
import pl.zielony.fragmentmanager.FragmentTransaction;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null)
            getFragmentManager2().add(MainFragment.class, R.id.root, FragmentTransaction.Mode.Join);
    }

}
