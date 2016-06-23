package pl.zielony.fragmentmanager;

/**
 * Created by Marcin on 2015-12-15.
 */
public interface FragmentManagerInterface {

    <T extends Fragment> T add(T fragment, int id, FragmentTransaction.Mode mode);

    <T extends Fragment> T add(T fragment, String tag, FragmentTransaction.Mode mode);

    <T extends Fragment> T add(Class<T> fragmentClass, int id, FragmentTransaction.Mode mode);

    <T extends Fragment> T add(Class<T> fragmentClass, String tag, FragmentTransaction.Mode mode);

    <T extends Fragment> T replace(T addFragment, int id, FragmentTransaction.Mode mode);

    <T extends Fragment> T replace(T addFragment, String tag, FragmentTransaction.Mode mode);

    <T extends Fragment, T2 extends Fragment> T replace(T2 removeFragment, T addFragment, FragmentTransaction.Mode mode);

    <T extends Fragment> T replace(Class<T> fragmentClass, String tag, FragmentTransaction.Mode mode);

    <T extends Fragment> T replace(Class<T> fragmentClass, int id, FragmentTransaction.Mode mode);

    <T extends Fragment, T2 extends Fragment> T replace(T2 removeFragment, Class<T> fragmentClass, FragmentTransaction.Mode mode);

    void remove(int id, FragmentTransaction.Mode mode);

    void remove(String tag, FragmentTransaction.Mode mode);

    <T extends Fragment> void remove(T fragment, FragmentTransaction.Mode mode);

    boolean up();

    boolean back();

    boolean hasBack();

    boolean hasUp();
}
