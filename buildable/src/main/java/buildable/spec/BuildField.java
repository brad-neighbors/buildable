package buildable.spec;


import buildable.annotation.BuiltWith;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a customization for a field in the builder. This is done by specifying the name of the field and supplying a @BuiltWith annotation. This
 * is similar to the field in the class being annotated with @BuiltWith.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
public @interface BuildField {

    String name();

    BuiltWith value();
}
