package pl.zielony.fragmentmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

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
        if (fragmentManager.back())
            return;
        super.onBackPressed();
    }

    public void onUpPressed() {
        fragmentManager.up();
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

    @Override
    protected void onStart() {
        super.onStart();
        fragmentManager.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fragmentManager.onResume();
    }

    @Override
    protected void onPause() {
        fragmentManager.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        fragmentManager.onStop();
        super.onStop();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        fragmentManager.onNewIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fragmentManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return fragmentManager.onKeyEvent(event) || super.dispatchKeyEvent(event);
    }
}
