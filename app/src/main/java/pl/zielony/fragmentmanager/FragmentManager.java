package pl.zielony.fragmentmanager;

import android.app.Activity;
import android.os.Bundle;
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

    public FragmentManager(Activity activity, ViewGroup root) {
        this.activity = activity;
        roots = new ArrayList<>();
        roots.add(root);
        backstack = new ArrayList<>();
    }

    public void push(Class<? extends Fragment> fragmentClass, final int id) {
        push(fragmentClass, id, null);
    }

    public void push(Class<? extends Fragment> fragmentClass, String tag) {
        push(fragmentClass, 0, tag);
    }

    private void push(Class<? extends Fragment> fragmentClass, final int id, String tag) {
        FragmentTransaction prevTransaction = backstack.get(backstack.size() - 1);
        prevTransaction.fragment.pause();

        Fragment fragment = instantiate(fragmentClass);
        final FragmentTransaction transaction = new FragmentTransaction(fragment, id, tag, true);
        backstack.add(transaction);

        getContainer(transaction).addView(fragment.getView());
        fragment.animateInAdd(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                getContainer(transaction).removeAllViews();
            }
        });
        fragment.resume();
    }

    public boolean up() {
        if (!hasUp())
            return false;
        FragmentTransaction transaction = backstack.remove(backstack.size() - 1);
        transaction.fragment.pause();
        transaction.fragment = null;

        FragmentTransaction prevTransaction = backstack.get(backstack.size() - 1);
        getContainer(transaction).removeAllViews();
        prevTransaction.fragment = instantiate(prevTransaction.fragmentClass);
        getContainer(prevTransaction).addView(prevTransaction.fragment.getView());
        prevTransaction.fragment.resume();
        return true;
    }

    public <T extends Fragment> T add(Class<T> fragmentClass, int id) {
        return add(fragmentClass, id, null);
    }

    public <T extends Fragment> T add(Class<T> fragmentClass, String tag) {
        return add(fragmentClass, 0, tag);
    }

    private <T extends Fragment> T add(Class<T> fragmentClass, final int id, String tag) {
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
        Fragment fragment = instantiate(fragmentClass);
        FragmentTransaction transaction = new FragmentTransaction(fragment, id, tag, false);
        backstack.add(transaction);
        getContainer(transaction).addView(fragment.getView());
        fragment.animateInAdd(null);
        fragment.resume();
        return (T) fragment;
    }

    public boolean back() {
        if (!hasBack())
            return false;
        final FragmentTransaction transaction = backstack.remove(backstack.size() - 1);
        transaction.fragment.pause();
        transaction.fragment.animateOutBack(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                getContainer(transaction).removeView(transaction.fragment.getView());
                transaction.fragment = null;
            }
        });

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
        return backstack.size() > 1;
    }

    public boolean hasUp() {
        for (FragmentTransaction transaction : backstack) {
            if (transaction.backstack)
                return true;
        }
        return false;
    }

    public void clear() {
        backstack = new ArrayList<>();
        throw new RuntimeException("Not implemented");
    }

    public void save(Bundle bundle) {
        /*ArrayList<Bundle> managerBundle = new ArrayList<>();
        List<Fragment> list = backstack.get(0);
        for (int j = 0; j < list.size(); j++) {
            Fragment fragment = list.get(j);
            Bundle fragmentBundle = new Bundle();
            fragment.onSaveState(fragmentBundle);
            fragmentBundle.putString("class", fragment.getClass().getName());
            managerBundle.add(fragmentBundle);
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
        return null;
    }

    private Fragment instantiate(Class<? extends Fragment> fragmentClass){
        Fragment fragment = null;
        try {
            fragment = fragmentClass.getConstructor(Activity.class,FragmentManager.class).newInstance(activity,this);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return fragment;
    }
}
