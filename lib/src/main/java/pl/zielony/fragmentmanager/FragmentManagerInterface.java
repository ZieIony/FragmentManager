package pl.zielony.fragmentmanager;

/**
 * Created by Marcin on 2015-12-15.
 */
public interface FragmentManagerInterface {

    <T extends Fragment> T add(T fragment, int id, FragmentState.Mode mode);

    <T extends Fragment> T add(T fragment, String tag, FragmentState.Mode mode);

    <T extends Fragment> T add(Class<T> fragmentClass, int id, FragmentState.Mode mode);

    <T extends Fragment> T add(Class<T> fragmentClass, String tag, FragmentState.Mode mode);

    <T extends Fragment> T replace(T addFragment, int id, FragmentState.Mode mode);

    <T extends Fragment> T replace(T addFragment, String tag, FragmentState.Mode mode);

    <T extends Fragment, T2 extends Fragment> T replace(T2 removeFragment, T addFragment, FragmentState.Mode mode);

    <T extends Fragment> T replace(Class<T> fragmentClass, String tag, FragmentState.Mode mode);

    <T extends Fragment> T replace(Class<T> fragmentClass, int id, FragmentState.Mode mode);

    <T extends Fragment, T2 extends Fragment> T replace(T2 removeFragment, Class<T> fragmentClass, FragmentState.Mode mode);

    void remove(int id, FragmentState.Mode mode);

    void remove(String tag, FragmentState.Mode mode);

    <T extends Fragment> void remove(T fragment, FragmentState.Mode mode);

    boolean up();

    boolean back();

    boolean hasBack();

    boolean hasUp();
}
