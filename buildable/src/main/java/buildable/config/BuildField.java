package buildable.config;


import buildable.annotation.BuiltWith;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.SOURCE)
public @interface BuildField {

    String name();

    BuiltWith value();
}
