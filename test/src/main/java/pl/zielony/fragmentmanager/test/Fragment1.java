package pl.zielony.fragmentmanager.test;

import android.os.Parcelable;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;

import pl.zielony.fragmentmanager.Fragment;
import pl.zielony.fragmentmanager.FragmentManager;

/**
 * Created by Marcin on 2015-12-08.
 */
public class Fragment1 extends Fragment {
    public Fragment1(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    protected View onCreateView() {
        View view = View.inflate(getContext(), R.layout.fragment1, null);
        final EditText et = (EditText) view.findViewById(R.id.et);
        final SparseArray<Parcelable> container = new SparseArray<Parcelable>();
        view.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et.saveHierarchyState(container);
            }
        });
        view.findViewById(R.id.restore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et.restoreHierarchyState(container);
            }
        });
        return view;
    }
}
