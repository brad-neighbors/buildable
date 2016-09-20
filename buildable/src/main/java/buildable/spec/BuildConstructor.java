package buildable.spec;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that the builder for the type should use a specified constructor.
 * The arguments of the constructor to be used is specified via {@link ConstructorArg}.
 * The order of the arguments is taken from the order of the {@link ConstructorArg} annotations.
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface BuildConstructor {

    ConstructorArg[] value() default {};
}
