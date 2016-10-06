package pl.zielony.fragmentmanager;

import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.nineoldandroids.animation.ArgbEvaluator;

/**
 * Created by Marcin on 2016-08-08.
 */

public class TextViewSharedElement extends SharedElement<TextViewKeyFrame, TextView> {
    private ArgbEvaluator evaluator = new ArgbEvaluator();

    public TextViewSharedElement(View view, Fragment from, Fragment to) {
        this.idFrom = from.getId();
        this.idTo = to.getId();
        this.viewId = view.getId();
    }

    public TextViewSharedElement(int id, Fragment from, Fragment to) {
        this.idFrom = from.getId();
        this.idTo = to.getId();
        this.viewId = id;
    }

    @Override
    public void onUpdate(float interpolation) {
        view.setTextColor((Integer) evaluator.evaluate(interpolation, frameFrom.textColor, frameTo.textColor));
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, lerp(frameFrom.textSize, frameTo.textSize, interpolation));
        super.onUpdate(interpolation);
    }

    protected TextViewKeyFrame setupFrame(View view, int[] containerLocation) {
        KeyFrame frame = new TextViewKeyFrame();
        ((TextViewKeyFrame) frame).textColor = ((TextView) view).getCurrentTextColor();
        ((TextViewKeyFrame) frame).textSize = ((TextView) view).getTextSize();
        final int[] viewLocation = new int[2];
        view.getLocationOnScreen(viewLocation);
        frame.rect.set(0, 0, view.getWidth(), view.getHeight());
        frame.rect.offset(viewLocation[0] - containerLocation[0], viewLocation[1] - containerLocation[1]);
        return (TextViewKeyFrame) frame;
    }
}
