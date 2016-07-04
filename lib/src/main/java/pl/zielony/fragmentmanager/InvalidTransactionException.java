package pl.zielony.fragmentmanager;

/**
 * Created by Marcin on 2016-07-01.
 */

public class InvalidTransactionException extends RuntimeException {
    public InvalidTransactionException() {
        super("Cannot add a fragment to another fragment using one fragmentManager. Use child fragmentManager instead");
    }

    public InvalidTransactionException(String s) {
        super(s);
    }
}
