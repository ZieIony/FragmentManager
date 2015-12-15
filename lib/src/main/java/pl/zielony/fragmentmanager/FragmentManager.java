package pl.zielony.fragmentmanager;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marcin on 2015-03-20.
 */
public class FragmentManager {
    private final Activity activity;
    List<View> roots;
    List<FragmentTransaction> backstack;
    boolean restoring = false;

    public FragmentManager(Activity activity) {
        this.activity = activity;
        roots = new ArrayList<>();
        roots.add(activity.getWindow().getDecorView().getRootView());
        backstack = new ArrayList<>();
    }

    public <T extends Fragment> T push(T fragment, final int id) {
        return push(null, fragment, id, null);
    }

    public <T extends Fragment> T push(T fragment, String tag) {
        return push(null, fragment, 0, tag);
    }

    public <T extends Fragment> T push(Class<T> fragmentClass, final int id) {
        T fragment = instantiate(fragmentClass);
        return push(null, fragment, id, null);
    }

    public <T extends Fragment> T push(Class<T> fragmentClass, String tag) {
        T fragment = instantiate(fragmentClass);
        return push(null, fragment, 0, tag);
    }

    public <T extends Fragment> T push(Fragment parent, T fragment, final int id) {
        return push(parent, fragment, id, null);
    }

    public <T extends Fragment> T push(Fragment parent, T fragment, String tag) {
        return push(parent, fragment, 0, tag);
    }

    public <T extends Fragment> T push(Fragment parent, Class<T> fragmentClass, final int id) {
        T fragment = instantiate(fragmentClass);
        return push(parent, fragment, id, null);
    }

    public <T extends Fragment> T push(Fragment parent, Class<T> fragmentClass, String tag) {
        T fragment = instantiate(fragmentClass);
        return push(parent, fragment, 0, tag);
    }

    public <T extends Fragment> T add(T fragment, int id) {
        return add(null, fragment, id, null);
    }

    public <T extends Fragment> T add(T fragment, String tag) {
        return add(null, fragment, 0, tag);
    }

    public <T extends Fragment> T add(Class<T> fragmentClass, int id) {
        T fragment = instantiate(fragmentClass);
        return add(null, fragment, id, null);
    }

    public <T extends Fragment> T add(Class<T> fragmentClass, String tag) {
        T fragment = instantiate(fragmentClass);
        return add(null, fragment, 0, tag);
    }

    public <T extends Fragment> T add(Fragment parent, T fragment, int id) {
        return add(parent, fragment, id, null);
    }

    public <T extends Fragment> T add(Fragment parent, T fragment, String tag) {
        return add(null, fragment, 0, tag);
    }

    public <T extends Fragment> T add(Fragment parent, Class<T> fragmentClass, int id) {
        T fragment = instantiate(fragmentClass);
        return add(parent, fragment, id, null);
    }

    public <T extends Fragment> T add(Fragment parent, Class<T> fragmentClass, String tag) {
        T fragment = instantiate(fragmentClass);
        return add(parent, fragment, 0, tag);
    }

    public <T extends Fragment> T join(T fragment, int id) {
        return join(fragment, id, null);
    }

    public <T extends Fragment> T join(T fragment, String tag) {
        return join(fragment, 0, tag);
    }

    public <T extends Fragment> T join(Class<T> fragmentClass, int id) {
        T fragment = instantiate(fragmentClass);
        return join(fragment, id, null);
    }

    public <T extends Fragment> T join(Class<T> fragmentClass, String tag) {
        T fragment = instantiate(fragmentClass);
        return join(fragment, 0, tag);
    }

    private <T extends Fragment> T push(Fragment parent, T fragment, final int id, String tag) {
        FragmentTransaction prevTransaction = backstack.get(backstack.size() - 1);
        prevTransaction.fragment.pause();

        final FragmentTransaction transaction = new FragmentTransaction(getParentTransaction(parent), fragment, id, tag, FragmentTransaction.Mode.Push);
        backstack.add(transaction);

        getContainer(transaction).addView(fragment.getView());
        fragment.animateInAdd(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                getContainer(transaction).removeAllViews();
            }
        });
        fragment.resume();
        return fragment;
    }

    private <T extends Fragment> T add(Fragment parent, T fragment, final int id, String tag) {
        if (hasBack()) {
            for (int i = backstack.size() - 1; i >= 0; i--) {
                final FragmentTransaction transaction = backstack.get(i);
                final Fragment prevFragment = transaction.fragment;
                prevFragment.pause();
                prevFragment.animateOutAdd(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        getContainer(transaction).removeView(prevFragment.getView());
                        transaction.fragment = null;
                    }
                });
                if (transaction.id == id || tag != null && tag.equals(transaction.tag))
                    break;
            }
        }
        FragmentTransaction transaction = new FragmentTransaction(getParentTransaction(parent), fragment, id, tag, FragmentTransaction.Mode.Add);
        backstack.add(transaction);
        getContainer(transaction).addView(fragment.getView());
        fragment.animateInAdd(null);
        fragment.resume();
        return (T) fragment;
    }

    private <T extends Fragment> T join(T fragment, final int id, String tag) {
        FragmentTransaction transaction = new FragmentTransaction(null, fragment, id, tag, FragmentTransaction.Mode.Join);
        backstack.add(transaction);
        getContainer(transaction).addView(fragment.getView());
        fragment.animateInAdd(null);
        fragment.resume();
        return (T) fragment;
    }

    public boolean up() {
        if (!hasUp())
            return false;
        FragmentTransaction transaction;
        while (true) {
            transaction = backstack.remove(backstack.size() - 1);
            transaction.fragment.pause();
            final FragmentTransaction finalTransaction = transaction;
            transaction.fragment.animateOutBack(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    getContainer(finalTransaction).removeView(finalTransaction.fragment.getView());
                    finalTransaction.fragment = null;
                }
            });
            if (transaction.mode == FragmentTransaction.Mode.Push)
                break;
        }

        FragmentTransaction prevTransaction = backstack.get(backstack.size() - 1);
        getContainer(transaction).removeAllViews();
        prevTransaction.fragment = instantiate(prevTransaction.fragmentClass);
        getContainer(prevTransaction).addView(prevTransaction.fragment.getView());
        prevTransaction.fragment.resume();
        return true;
    }

    public boolean back() {
        if (!hasBack())
            return false;
        FragmentTransaction transaction;
        while (true) {
            transaction = backstack.remove(backstack.size() - 1);
            transaction.fragment.pause();
            final FragmentTransaction finalTransaction = transaction;
            transaction.fragment.animateOutBack(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    getContainer(finalTransaction).removeView(finalTransaction.fragment.getView());
                    finalTransaction.fragment = null;
                }
            });
            if (transaction.mode != FragmentTransaction.Mode.Join)
                break;
        }

        FragmentTransaction prevTransaction = backstack.get(backstack.size() - 1);
        if (prevTransaction.id == transaction.id) {
            prevTransaction.fragment = instantiate(prevTransaction.fragmentClass);
            getContainer(prevTransaction).addView(prevTransaction.fragment.getView());
            prevTransaction.fragment.animateInBack();
            prevTransaction.fragment.resume();
        }
        return true;
    }

    public boolean hasBack() {
        for (int i = backstack.size() - 1; i >= 0; i--) {
            FragmentTransaction transaction = backstack.get(i);
            if (transaction.mode == FragmentTransaction.Mode.Join)
                continue;
            if (i > 0)
                return true;
        }

        return false;
    }

    public boolean hasUp() {
        for (int i = backstack.size() - 1; i >= 0; i--) {
            FragmentTransaction transaction = backstack.get(i);
            if (transaction.mode != FragmentTransaction.Mode.Push)
                continue;
            if (i > 0)
                return true;
        }

        return false;
    }

    public void clear() {
        backstack = new ArrayList<>();
        throw new RuntimeException("Not implemented");
    }

    public void save(Bundle bundle) {
        ArrayList<Bundle> managerBundle = new ArrayList<>();
        for (int j = 0; j < backstack.size(); j++) {
            FragmentTransaction transaction = backstack.get(j);
            Bundle fragmentBundle = new Bundle();
            transaction.onSaveState(fragmentBundle);
            managerBundle.add(fragmentBundle);
        }
        bundle.putParcelableArrayList("fragmentManagerBackstack", managerBundle);
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
    private ViewGroup getContainer(FragmentTransaction transaction) {
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
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return (T) fragment;
    }

    public Activity getActivity() {
        return activity;
    }

    private FragmentTransaction getParentTransaction(Fragment parent) {
        if (parent == null)
            return null;
        FragmentTransaction parentTransaction = null;
        for (int i = backstack.size() - 1; i >= 0; i--) {
            if (backstack.get(i).fragment == parent) {
                parentTransaction = backstack.get(i);
                break;
            }
        }
        return parentTransaction;
    }
}
