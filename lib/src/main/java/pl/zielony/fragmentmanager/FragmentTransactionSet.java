package pl.zielony.fragmentmanager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marcin on 2015-12-31.
 */
public class FragmentTransactionSet implements FragmentTransaction {
    List<FragmentTransaction> list = new ArrayList<>();

    public void addTransaction(FragmentTransaction transaction){
        list.add(transaction);
    }

    @Override
    public void execute() {
        for(FragmentTransaction transaction:list)
            transaction.execute();
    }

    @Override
    public void undo() {
        for(FragmentTransaction transaction:list)
            transaction.undo();
    }
}
