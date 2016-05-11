package pl.zielony.fragmentmanager;

/**
 * Created by Marcin on 2015-12-15.
 */
public interface FragmentManagerInterface {

    <T extends Fragment> T add(T fragment, int id);

    <T extends Fragment> T add(T fragment, String tag);

    <T extends Fragment> T add(Class<T> fragmentClass, int id);

    <T extends Fragment> T add(Class<T> fragmentClass, String tag);

    <T extends Fragment> T push(T fragment, int id);

    <T extends Fragment> T push(T fragment, String tag);

    <T extends Fragment> T push(Class<T> fragmentClass, int id);

    <T extends Fragment> T push(Class<T> fragmentClass, String tag);

    <T extends Fragment> T join(T fragment, int id);

    <T extends Fragment> T join(T fragment, String tag);

    <T extends Fragment> T join(Class<T> fragmentClass, int id);

    <T extends Fragment> T join(Class<T> fragmentClass, String tag);

    <T extends Fragment> T dialog(T fragment, int id);

    <T extends Fragment> T dialog(T fragment, String tag);

    <T extends Fragment> T dialog(Class<T> fragmentClass, int id);

    <T extends Fragment> T dialog(Class<T> fragmentClass, String tag);

    boolean up();

    boolean back();

    boolean hasBack();

    boolean hasUp();
}
