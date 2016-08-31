package buildable.config;

import buildable.annotation.Buildable;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface InjectBuildable {

    String[] excludedFields() default {};

    Buildable value() default @Buildable;

    BuildField[] fields() default {};
}
