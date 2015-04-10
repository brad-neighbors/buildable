/*
 * Project vCloud Air
 * Copyright (c) 2015 VMware, Inc. All rights reserved.
 * VMware Confidential.
 */
package buildable.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that a particular field in an @Buildable will not have a fluent builder method
 * in the generated Builder class.
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface ExcludeFromBuilder {
}
