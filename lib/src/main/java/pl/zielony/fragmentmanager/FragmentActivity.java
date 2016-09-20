package pl.zielony.fragmentmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Marcin on 2016-05-11.
 */
public class FragmentActivity extends AppCompatActivity {
    private FragmentManager fragmentManager;
    private FragmentRootView rootView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = new FragmentManager(this);
        fragmentManager.setRoot(rootView);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        rootView = new FragmentRootView(this);
        rootView.addView(view);
        super.setContentView(rootView, params);
    }

    @Override
    public void onBackPressed() {
        if (fragmentManager.backTraverse())
            return;
        super.onBackPressed();
    }

    public void onUpPressed() {
        fragmentManager.upTraverse();
    }

    public void navigate(FragmentRoute route) {
        FragmentRoute.RouteStep step = route.getStep();
        if (step.fragment == null)
            step.fragment = Fragment.instantiate(step.klass, this);
        onNavigate(step.fragment, step.mode);
        fragmentManager.navigate(route);
    }

    protected boolean onNavigate(Fragment fragment, TransactionMode mode) {
        return false;
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
