package pl.zielony.fragmentmanager.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import pl.zielony.fragmentmanager.FragmentActivity;
import pl.zielony.fragmentmanager.FragmentManager;
import pl.zielony.fragmentmanager.FragmentState;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null)
            getFragmentManager2().add(MainFragment.class, R.id.root, FragmentState.Mode.Join);
    }

}
