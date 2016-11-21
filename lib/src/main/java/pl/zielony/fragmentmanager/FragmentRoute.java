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

    public static class Step {
        Class<? extends Fragment> klass;
        Fragment fragment;
        TransactionMode mode;

        Step(Class<? extends Fragment> klass, TransactionMode mode) {
            this.klass = klass;
            this.mode = mode;
        }

        Step(Fragment fragment, TransactionMode mode) {
            this.fragment = fragment;
            this.mode = mode;
        }

        public Fragment getFragment() {
            return fragment;
        }

        public TransactionMode getMode() {
            return mode;
        }
    }

    private List<Step> fragments = new ArrayList<>();

    public FragmentRoute(Class<? extends Fragment> klass, TransactionMode mode) {
        fragments.add(new Step(klass, mode));
    }

    public FragmentRoute(Fragment fragment, TransactionMode mode) {
        fragments.add(new Step(fragment, mode));
    }

    public void addFragment(Class<? extends Fragment> klass, TransactionMode mode) {
        fragments.add(new Step(klass, mode));
    }

    public void addFragment(Fragment fragment, TransactionMode mode) {
        fragments.add(new Step(fragment, mode));
    }

    public Step getStep() {
        return fragments.get(0);
    }

    public Step removeStep() {
        return fragments.remove(0);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < fragments.size(); i++) {
            Step step = fragments.get(i);
            builder.append(step.klass);
            if (i != fragments.size() - 1)
                builder.append(", ");
        }
        return builder.toString();
    }
}
