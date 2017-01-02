package pl.zielony.fragmentmanager;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import pl.zielony.animator.AnimatorListenerAdapter;

public class LockListenerAdapter extends AnimatorListenerAdapter {
    private final Drawable windowBackground;
    private FragmentRootView view;

    public LockListenerAdapter(FragmentRootView view) {
        this.view = view;
        windowBackground = getThemeColor(view.getContext(), android.R.attr.windowBackground);
    }

    private Drawable getThemeColor(Context context, int attr) {
        Resources.Theme theme = context.getTheme();
        TypedValue typedvalueattr = new TypedValue();
        theme.resolveAttribute(attr, typedvalueattr, true);
        return typedvalueattr.resourceId != 0 ? context.getResources().getDrawable(typedvalueattr.resourceId) : new ColorDrawable(typedvalueattr.data);
    }

    @Override
    public void onCancel() {
        view.setLocked(false);
        view.setBackgroundDrawable(null);
    }

    @Override
    public void onStart() {
        view.setLocked(true);
        view.setBackgroundDrawable(windowBackground);
    }

    @Override
    public void onEnd() {
        view.setLocked(false);
        view.setBackgroundDrawable(null);
    }

}
