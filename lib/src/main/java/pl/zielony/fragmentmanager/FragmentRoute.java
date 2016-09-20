package pl.zielony.fragmentmanager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marcin on 2016-09-20.
 */
public class FragmentRoute {
    private List<Class<? extends Fragment>> fragments = new ArrayList<>();

    public void addFragment(Class<? extends Fragment> klass){
        fragments.add(klass);
    }

    public Class<? extends Fragment> getFragment(){
        return fragments.remove(0);
    }
}
