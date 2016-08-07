package pl.zielony.fragmentmanager;

import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Marcin on 2016-05-11.
 */
public abstract class FragmentPagerAdapter extends PagerAdapter {
    private FragmentManager fragmentManager;
    private SparseArray<Fragment> fragments = new SparseArray<>();

    public FragmentPagerAdapter(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    public abstract Fragment getItem(int position);

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment item = fragments.get(position);
        if (item == null) {
            item = fragmentManager.getFragment(container + ";" + position);
            fragments.put(position, item);
        }
        if (item == null) {
            item = getItem(position);
            fragments.put(position, item);
        }
        fragmentManager.add(item, container.getId(), TransactionMode.Join);
        return item;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        fragmentManager.remove(fragments.get(position), TransactionMode.Join);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return ((Fragment) object).getRootView() == view;
    }
}
