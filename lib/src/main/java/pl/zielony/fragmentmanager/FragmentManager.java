package pl.zielony.fragmentmanager;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marcin on 2015-03-20.
 */
public class FragmentManager implements FragmentManagerInterface {
    private final Activity activity;
    List<View> roots;
    List<FragmentTransaction> backstack;
    List<FragmentState> activeStates;
    boolean restoring = false;
    FragmentManager parent;

    public FragmentManager(Activity activity, View root) {
        this.activity = activity;
        roots = new ArrayList<>();
        roots.add(root);
        backstack = new ArrayList<>();
        activeStates = new ArrayList<>();
    }

    public FragmentManager(FragmentManager parent, View root) {
        this.activity = parent.getActivity();
        roots = new ArrayList<>();
        roots.add(root);
        backstack = new ArrayList<>();
        activeStates = new ArrayList<>();
        this.parent = parent;
    }

    @Override
    public <T extends Fragment> T push(T fragment, final int id) {
        return push(fragment, id, null);
    }

    @Override
    public <T extends Fragment> T push(T fragment, String tag) {
        return push(fragment, 0, tag);
    }

    @Override
    public <T extends Fragment> T push(Class<T> fragmentClass, final int id) {
        T fragment = instantiate(fragmentClass);
        return push(fragment, id, null);
    }

    @Override
    public <T extends Fragment> T push(Class<T> fragmentClass, String tag) {
        T fragment = instantiate(fragmentClass);
        return push(fragment, 0, tag);
    }

    @Override
    public <T extends Fragment> T add(T fragment, int id) {
        return add(fragment, id, null);
    }

    @Override
    public <T extends Fragment> T add(T fragment, String tag) {
        return add(fragment, 0, tag);
    }

    @Override
    public <T extends Fragment> T add(Class<T> fragmentClass, int id) {
        T fragment = instantiate(fragmentClass);
        return add(fragment, id, null);
    }

    @Override
    public <T extends Fragment> T add(Class<T> fragmentClass, String tag) {
        T fragment = instantiate(fragmentClass);
        return add(fragment, 0, tag);
    }

    @Override
    public <T extends Fragment> T join(T fragment, int id) {
        return join(fragment, id, null);
    }

    @Override
    public <T extends Fragment> T join(T fragment, String tag) {
        return join(fragment, 0, tag);
    }

    @Override
    public <T extends Fragment> T join(Class<T> fragmentClass, int id) {
        T fragment = instantiate(fragmentClass);
        return join(fragment, id, null);
    }

    @Override
    public <T extends Fragment> T join(Class<T> fragmentClass, String tag) {
        T fragment = instantiate(fragmentClass);
        return join(fragment, 0, tag);
    }

    private <T extends Fragment> T push(T fragment, final int id, String tag) {
        FragmentTransactionSet transactionSet = new FragmentTransactionSet();

        for (int i = activeStates.size() - 1; i >= 0; i--) {
            final FragmentState state = activeStates.get(i);
            if (state.id == id || tag != null && tag.equals(state.tag))
                transactionSet.addTransaction(new RemoveFragmentTransaction(state, this));
        }

        transactionSet.addTransaction(new AddFragmentTransaction(new FragmentState(fragment, id, tag, FragmentState.Mode.Push), this));
        backstack.add(transactionSet);
        transactionSet.execute();

        return (T) fragment;
    }

    private <T extends Fragment> T add(T fragment, final int id, String tag) {
        FragmentTransactionSet transactionSet = new FragmentTransactionSet();

        for (int i = activeStates.size() - 1; i >= 0; i--) {
            final FragmentState state = activeStates.get(i);
            if (state.id == id || tag != null && tag.equals(state.tag))
                transactionSet.addTransaction(new RemoveFragmentTransaction(state, this));
        }

        transactionSet.addTransaction(new AddFragmentTransaction(new FragmentState(fragment, id, tag, FragmentState.Mode.Add), this));
        backstack.add(transactionSet);
        transactionSet.execute();

        return (T) fragment;
    }

    private <T extends Fragment> T join(T fragment, final int id, String tag) {
        inAddState(new FragmentState(fragment, id, tag, FragmentState.Mode.Join));
        return (T) fragment;
    }

    public boolean up() {
        for (int i = activeStates.size() - 1; i >= 0; i--) {
            if (activeStates.get(i).fragment.hasUp())
                if (activeStates.get(i).fragment.up())
                    return true;
        }
        while (backstack.size() != 0) {
            backstack.remove(backstack.size() - 1).undo();
            if (activeStates.isEmpty())
                break;
            FragmentState state = activeStates.get(activeStates.size() - 1);
            if (state.mode == FragmentState.Mode.Push)
                return true;
        }
        return false;
    }

    /**
     * Pops one backstack step
     *
     * @return if back could pop one complete step
     */
    public boolean back() {
        for (int i = activeStates.size() - 1; i >= 0; i--) {
            if (activeStates.get(i).fragment.hasBack())
                if (activeStates.get(i).fragment.back())
                    return true;
        }
        while (backstack.size() != 0) {
            backstack.remove(backstack.size() - 1).undo();
            if (activeStates.isEmpty())
                break;
            FragmentState state = activeStates.get(activeStates.size() - 1);
            if (state.mode != FragmentState.Mode.Join)
                return true;
        }
        return false;
    }

    void inAddState(FragmentState state) {
        Fragment fragment = state.fragment;
        activeStates.add(state);
        getContainer(state).addView(fragment.getView());
        fragment.resume();
        fragment.animateInAdd();
    }

    void inBackState(FragmentState state) {
        if (state.fragment == null)
            state.fragment = instantiate(state.fragmentClass);
        Fragment fragment = state.fragment;
        activeStates.add(state);
        getContainer(state).addView(fragment.getView());
        restoring=true;
        fragment.onRestoreState(state.state);
        fragment.resume();
        fragment.animateInBack();
        restoring=false;
    }

    void outAddState(final FragmentState state) {
        final Fragment fragment = state.fragment;
        activeStates.remove(state);
        fragment.animateOutAdd(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fragment.pause();
                fragment.onSaveState(state.state);
                getContainer(state).removeView(fragment.getView());
                state.fragment = null;
            }
        });
    }

    void outBackState(final FragmentState state) {
        final Fragment fragment = state.fragment;
        activeStates.remove(state);
        state.fragment.animateOutBack(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fragment.pause();
                getContainer(state).removeView(fragment.getView());
                state.fragment = null;
            }
        });
    }

    public boolean hasBack() {
        for (int i = activeStates.size() - 1; i >= 0; i--) {
            FragmentState state = activeStates.get(i);
            if (state.mode == FragmentState.Mode.Join)
                continue;
            if (i > 0)
                return true;
        }

        return false;
    }

    public boolean hasUp() {
        for (int i = activeStates.size() - 1; i >= 0; i--) {
            FragmentState transaction = activeStates.get(i);
            if (transaction.mode != FragmentState.Mode.Push)
                continue;
            if (i > 0)
                return true;
        }

        return false;
    }

    public void clear() {
        while (!backstack.isEmpty()) {
            FragmentTransaction transaction = backstack.remove(backstack.size() - 1);
            transaction.undo();
        }
    }

    public void save(Bundle bundle) {
      /*  ArrayList<Bundle> managerBundle = new ArrayList<>();
        for (int j = 0; j < backstack.size(); j++) {
            FragmentState transaction = backstack.get(j);
            transaction.onSaveState();
            managerBundle.add(transaction.state);
        }
        bundle.putParcelableArrayList("fragmentManagerBackstack", managerBundle);*/
    }

    public void restore(Bundle bundle) {
        restoring = true;

        /*clear();
        ArrayList<Bundle> managerBundle = bundle.getParcelableArrayList("fragmentManagerBackstack");
        if (managerBundle == null)
            return;
        List<Fragment> list = backstack.get(0);
        for (int i = 0; i < managerBundle.size(); i++) {
            Bundle fragmentBundle = managerBundle.get(i);
            try {
                Class fragmentClass = Class.forName(fragmentBundle.getString("class"));
                Fragment fragment = (Fragment) fragmentClass.getConstructor(Activity.class, FragmentManager.class).newInstance(container.getContext(), this);
                fragment.onRestoreState(fragmentBundle);
                list.add(fragment);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        Fragment fragment = list.get(list.size() - 1);
        container.addView(fragment.getView());
        fragment.onResume();*/

        restoring = false;
    }

    public boolean isRestoring() {
        return restoring;
    }

    @NonNull
    private ViewGroup getContainer(FragmentState transaction) {
        if (transaction.id != 0) {
            for (int i = roots.size() - 1; i >= 0; i--) {
                View v = roots.get(i).findViewById(transaction.id);
                if (v != null)
                    return (ViewGroup) v;
            }
        } else if (transaction.tag != null) {
            for (int i = roots.size() - 1; i >= 0; i--) {
                View v = roots.get(i).findViewWithTag(transaction.tag);
                if (v != null)
                    return (ViewGroup) v;
            }
        }
        return (ViewGroup) roots.get(0);
    }

    private <T extends Fragment> T instantiate(Class<T> fragmentClass) {
        Fragment fragment = null;
        try {
            fragment = fragmentClass.getConstructor(FragmentManager.class).newInstance(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return (T) fragment;
    }

    public Activity getActivity() {
        return activity;
    }

    public FragmentManager getParent() {
        return parent;
    }
}
