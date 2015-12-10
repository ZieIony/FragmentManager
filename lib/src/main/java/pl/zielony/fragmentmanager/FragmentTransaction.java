package pl.zielony.fragmentmanager;

import android.os.Bundle;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by Marcin on 2015-12-02.
 */
public class FragmentTransaction {
    enum Mode {
        Push, Add, Join
    }

    private static final String CLASS = "class", ID = "id", TAG = "tag", MODE = "mode", FRAGMENT = "fragment";

    public Class<? extends Fragment> fragmentClass;
    public Fragment fragment;
    public int id;
    public String tag;
    public Mode mode;

    public FragmentTransaction(Fragment fragment, int id, String tag, Mode mode) {
        this.fragment = fragment;
        this.fragmentClass = fragment.getClass();
        this.id = id;
        this.tag = tag;
        this.mode = mode;
    }

    public void onSaveState(Bundle state) {
        state.putString(CLASS, fragmentClass.getName());
        state.putInt(ID, id);
        state.putString(TAG, tag);
        state.putInt(MODE, mode.ordinal());
        Bundle fragmentBundle = new Bundle();
        fragment.onSaveState(fragmentBundle);
        state.putBundle(FRAGMENT, fragmentBundle);
    }

    public void onRestoreState(Bundle state, FragmentManager manager) {
        try {
            fragmentClass = (Class<? extends Fragment>) Class.forName(state.getString(CLASS));
            id = state.getInt(ID);
            tag = state.getString(TAG);
            mode = Mode.values()[state.getInt(MODE)];
            fragment = fragmentClass.getConstructor(FragmentManager.class).newInstance(manager);
            fragment.onRestoreState(state.getBundle(FRAGMENT));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
