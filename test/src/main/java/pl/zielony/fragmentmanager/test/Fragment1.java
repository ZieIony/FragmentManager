package pl.zielony.fragmentmanager.test;

import android.os.Parcelable;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;

import carbon.widget.TextView;
import pl.zielony.fragmentmanager.Fragment;
import pl.zielony.fragmentmanager.FragmentAnnotation;

/**
 * Created by Marcin on 2015-12-08.
 */
@FragmentAnnotation(layout = R.layout.fragment1, pooling = false)
public class Fragment1 extends Fragment {

    @Override
    protected void onCreate() {
        View view = getRootView();
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
}
