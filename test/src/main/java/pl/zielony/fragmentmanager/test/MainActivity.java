package pl.zielony.fragmentmanager.test;

import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import pl.zielony.fragmentmanager.FragmentManager;
import pl.zielony.fragmentmanager.FragmentState;

public class MainActivity extends AppCompatActivity {

    private FragmentManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager = new FragmentManager(this);

        if(savedInstanceState==null) {
            manager.join(MainFragment.class, R.id.root);
        }
    }

    @Override
    public void onBackPressed() {
        if(manager.hasBack()) {
            manager.back();
        }else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        manager.restore(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        manager.save(outState);
        super.onSaveInstanceState(outState);
    }
}
