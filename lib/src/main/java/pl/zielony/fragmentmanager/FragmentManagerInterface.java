package pl.zielony.fragmentmanager;

import android.content.Intent;

/**
 * Created by Marcin on 2015-12-15.
 */
public interface FragmentManagerInterface {

    <T extends Fragment> FragmentTransaction add(T fragment, int id, FragmentTransaction.Mode mode);

    <T extends Fragment> FragmentTransaction add(T fragment, String tag, FragmentTransaction.Mode mode);

    <T extends Fragment> FragmentTransaction add(Class<T> fragmentClass, int id, FragmentTransaction.Mode mode);

    <T extends Fragment> FragmentTransaction add(Class<T> fragmentClass, String tag, FragmentTransaction.Mode mode);

    <T extends Fragment> FragmentTransaction replace(T addFragment, int id, FragmentTransaction.Mode mode);

    <T extends Fragment> FragmentTransaction replace(T addFragment, String tag, FragmentTransaction.Mode mode);

    <T extends Fragment, T2 extends Fragment> FragmentTransaction replace(T2 removeFragment, T addFragment, FragmentTransaction.Mode mode);

    <T extends Fragment> FragmentTransaction replace(Class<T> fragmentClass, String tag, FragmentTransaction.Mode mode);

    <T extends Fragment> FragmentTransaction replace(Class<T> fragmentClass, int id, FragmentTransaction.Mode mode);

    <T extends Fragment, T2 extends Fragment> FragmentTransaction replace(T2 removeFragment, Class<T> fragmentClass, FragmentTransaction.Mode mode);

    FragmentTransaction remove(int id, FragmentTransaction.Mode mode);

    FragmentTransaction remove(String tag, FragmentTransaction.Mode mode);

    <T extends Fragment> FragmentTransaction remove(T fragment, FragmentTransaction.Mode mode);

    boolean up();

    boolean back();

    boolean hasBack();

    boolean hasUp();

}
