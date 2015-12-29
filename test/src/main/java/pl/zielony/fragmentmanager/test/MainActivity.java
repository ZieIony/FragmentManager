package pl.zielony.fragmentmanager.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import pl.zielony.fragmentmanager.FragmentManager;

public class MainActivity extends AppCompatActivity {

    private FragmentManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager = new FragmentManager(this,findViewById(R.id.root));
        manager.add(new MainFragment(manager),R.id.root);
    }

    @Override
    public void onBackPressed() {
        if(manager.hasBack()) {
            manager.back();
        }else{
            super.onBackPressed();
        }
    }
}
