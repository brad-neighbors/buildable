package com.incandescent.buildable.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that subclasses may participate in generation of Builder classes, if they themselves are marked <tt>@</tt>Buildable.
 *
 * Fields in classes (abstract or not) that are annotated with <tt>@</tt>BuildableSubclasses may be annotated with
 * <tt>@</tt>BuildWith, and when the concrete <tt>@</tt>Buildable class is processed,
 * it will have fields fluently buildable from both its own
 * fields and any superclasses marked with <tt>@</tt>BuildableSubclasses.
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface BuildableSubclasses {}
