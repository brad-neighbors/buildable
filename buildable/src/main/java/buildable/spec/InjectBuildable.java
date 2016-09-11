package buildable.spec;

import buildable.annotation.Buildable;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that a customization of the builder to be generated. This done by specifying an @Buildable annotation to customize the builder in the
 * same way using @Buildable on the class to build would.
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface InjectBuildable {

    String[] excludedFields() default {};

    Buildable value() default @Buildable;

    BuildField[] fields() default {};
}
