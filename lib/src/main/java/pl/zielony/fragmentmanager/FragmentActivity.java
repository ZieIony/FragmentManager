package pl.zielony.fragmentmanager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Marcin on 2016-05-11.
 */
public class FragmentActivity extends AppCompatActivity {
    public FragmentManager fragmentManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = new FragmentManager(this);
    }

    @Override
    public void onBackPressed() {
        if (fragmentManager.hasBack()) {
            fragmentManager.back();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        fragmentManager.save(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        fragmentManager.restore(savedInstanceState);
    }

    public FragmentManager getFragmentManager2() {
        return fragmentManager;
    }
}
