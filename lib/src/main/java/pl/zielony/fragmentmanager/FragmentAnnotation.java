package pl.zielony.fragmentmanager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Marcin on 2016-06-10.
 */
@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FragmentAnnotation {
    int layout() default 0;

    int title() default 0;

    boolean pooling() default true;

    Class<? extends FragmentAnimator> animator() default DefaultFragmentAnimator.class;
}
