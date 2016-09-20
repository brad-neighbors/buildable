package buildable.spec;


import buildable.annotation.BuiltWith;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that an argument to be used in the constructor in the build() method in the generated builder.
 * This generates a builder method as if the argument was a field.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
public @interface ConstructorArg {

    BuiltWith value() default @BuiltWith;

    String name();

    Class type();

}
