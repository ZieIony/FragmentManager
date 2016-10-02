package pl.zielony.fragmentmanager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marcin on 2016-09-20.
 */
public class FragmentRoute {
    public int length() {
        return fragments.size();
    }

    static class RouteStep {
        Class<? extends Fragment> klass;
        Fragment fragment;
        TransactionMode mode;

        RouteStep(Class<? extends Fragment> klass, TransactionMode mode) {
            this.klass = klass;
            this.mode = mode;
        }

        RouteStep(Fragment fragment, TransactionMode mode) {
            this.fragment = fragment;
            this.mode = mode;
        }
    }

    private List<RouteStep> fragments = new ArrayList<>();

    public FragmentRoute(Class<? extends Fragment> klass, TransactionMode mode) {
        fragments.add(new RouteStep(klass, mode));
    }

    public FragmentRoute(Fragment fragment, TransactionMode mode) {
        fragments.add(new RouteStep(fragment, mode));
    }

    public void addFragment(Class<? extends Fragment> klass, TransactionMode mode) {
        fragments.add(new RouteStep(klass, mode));
    }

    public void addFragment(Fragment fragment, TransactionMode mode) {
        fragments.add(new RouteStep(fragment, mode));
    }

    RouteStep removeStep() {
        return fragments.remove(0);
    }
}
