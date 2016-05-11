package pl.zielony.fragmentmanager;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Marcin on 2016-05-11.
 */
public abstract class FragmentPagerAdapter extends PagerAdapter {
    private FragmentManager fragmentManager;

    public FragmentPagerAdapter(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    public abstract Fragment getItem(int position);

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment item = getItem(position);
        fragmentManager.join(item, container.getId());
        return item;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeViewAt(position);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return ((Fragment) object).getView() == view;
    }
}
