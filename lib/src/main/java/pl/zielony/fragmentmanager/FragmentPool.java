package pl.zielony.fragmentmanager;

import android.app.Activity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Marcin on 2016-10-11.
 */

public class FragmentPool {
    private static Map<Class<? extends Fragment>, Fragment> fragmentPool = new HashMap<>();

    private FragmentPool(){

    }

    public static void put(Class<? extends Fragment> fragmentClass, Activity activity) {
        FragmentAnnotation annotation = fragmentClass.getAnnotation(FragmentAnnotation.class);
        if (annotation != null && !annotation.pooling())
            throw new RuntimeException(fragmentClass.getSimpleName() + " cannot be pooled because pooling is disabled by annotation");
        if (!fragmentPool.containsKey(fragmentClass))
            fragmentPool.put(fragmentClass, Fragment.instantiate(fragmentClass, activity, null));
    }

    public static void put(Fragment fragment) {
        if (!fragment.isPoolingEnabled())
            return;
        if (!fragmentPool.containsKey(fragment.getClass()))
            fragmentPool.put(fragment.getClass(), fragment);
    }

    // wyczyść basen :D
    public static void clear() {
        fragmentPool.clear();
    }

    public static <T extends Fragment> Fragment remove(Class<T> fragmentClass) {
        return fragmentPool.remove(fragmentClass);
    }
}
