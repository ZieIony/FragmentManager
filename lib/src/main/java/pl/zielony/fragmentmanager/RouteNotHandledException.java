package pl.zielony.fragmentmanager;

/**
 * Created by Marcin on 2016-11-02.
 */
public class RouteNotHandledException extends RuntimeException {
    public RouteNotHandledException(FragmentRoute s) {
        super(s.toString());
    }
}
