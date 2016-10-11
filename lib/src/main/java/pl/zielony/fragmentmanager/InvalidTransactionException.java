package pl.zielony.fragmentmanager;

/**
 * Created by Marcin on 2016-07-01.
 */

public class InvalidTransactionException extends RuntimeException {
    public InvalidTransactionException(String s) {
        super(s);
    }
}
