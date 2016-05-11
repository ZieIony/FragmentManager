package pl.zielony.fragmentmanager;

/**
 * Created by Marcin on 2015-12-15.
 */
public interface FragmentManagerInterface {

    <T extends Fragment> T add(T fragment, int id,FragmentState.Mode mode);

    <T extends Fragment> T add(T fragment, String tag,FragmentState.Mode mode);

    <T extends Fragment> T add(Class<T> fragmentClass, int id,FragmentState.Mode mode);

    <T extends Fragment> T add(Class<T> fragmentClass, String tag,FragmentState.Mode mode);

    <T extends Fragment> T replace(T fragment, int id,FragmentState.Mode mode);

    <T extends Fragment> T replace(T fragment, String tag,FragmentState.Mode mode);

    <T extends Fragment> T replace(Class<T> fragmentClass, int id,FragmentState.Mode mode);

    <T extends Fragment> T replace(Class<T> fragmentClass, String tag,FragmentState.Mode mode);

    <T extends Fragment> T remove(T fragment,int id,FragmentState.Mode mode);

    <T extends Fragment> T remove(T fragment,String tag, FragmentState.Mode mode);

    boolean up();

    boolean back();

    boolean hasBack();

    boolean hasUp();
}
