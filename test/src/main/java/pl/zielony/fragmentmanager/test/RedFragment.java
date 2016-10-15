package pl.zielony.fragmentmanager.test;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;

import java.util.Random;

import carbon.widget.TextView;
import pl.zielony.fragmentmanager.Fragment;
import pl.zielony.fragmentmanager.FragmentAnnotation;

/**
 * Created by Marcin on 2015-12-08.
 */
@FragmentAnnotation(layout = R.layout.fragment_red)
public class RedFragment extends Fragment {

    private static final String COLOR = "color";
    private int color;

    @Override
    protected void onCreate() {
        View view = getView();

        if (getState() == null) {
            int[] colors = {Color.RED, Color.BLUE, Color.GRAY, Color.BLACK, Color.YELLOW, Color.GREEN};
            color = colors[new Random().nextInt(colors.length)];
            view.setBackgroundColor(color);
        }else{
            color = getState().getInt(COLOR);
            getView().setBackgroundColor(color);
        }

        final EditText et = (EditText) view.findViewById(R.id.et);
        final TextView copy = (TextView) view.findViewById(R.id.copy);
        final SparseArray<Parcelable> container = new SparseArray<Parcelable>();
        view.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copy.setText(et.getText().toString());
                getRootView().saveHierarchyState(container);
            }
        });
        view.findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copy.setText("");
            }
        });
        view.findViewById(R.id.restore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getRootView().restoreHierarchyState(container);
            }
        });
    }

    @Override
    protected Bundle onSaveState() {
        Bundle state = new Bundle();
        state.putInt(COLOR, color);
        return state;
    }

}
