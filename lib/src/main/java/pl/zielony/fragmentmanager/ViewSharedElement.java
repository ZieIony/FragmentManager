package pl.zielony.fragmentmanager;

import android.view.View;

/**
 * Created by Marcin on 2016-08-08.
 */

public class ViewSharedElement extends SharedElement<KeyFrame, View> {
    public ViewSharedElement(View view, Fragment from, Fragment to) {
        this.idFrom = from.getId();
        this.idTo = to.getId();
        this.viewId = view.getId();
    }

    public ViewSharedElement(int id, Fragment from, Fragment to) {
        this.idFrom = from.getId();
        this.idTo = to.getId();
        this.viewId = id;
    }

    @Override
    protected KeyFrame setupFrame(View view, int[] containerLocation) {
        KeyFrame frame = new KeyFrame();
        final int[] viewLocation = new int[2];
        view.getLocationOnScreen(viewLocation);
        frame.rect.set(0, 0, view.getWidth(), view.getHeight());
        frame.rect.offset(viewLocation[0] - containerLocation[0], viewLocation[1] - containerLocation[1]);
        return frame;
    }
}
