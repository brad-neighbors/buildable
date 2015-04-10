package buildable.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that specifies fields in an @Buildable may be assigned using a fluent api approach in the
 * generated Builder.
 * <p/>
 * Use the methodName() to specify the name of the fluent method that will be generated in the Builder.
 * <p/>
 * Fluently chain together calls for an expressive build, such as:
 * </p>
 * Given a class:
 * <pre>
 *     package com.acme;
 *
 *     <tt>@</tt>Buildable(name="UserBuilder", factoryMethod="aUser")
 *     public class User {
 *
 *         <tt>@</tt>BuiltWith(methodName="named")
 *         private String name;
 *
 *         <tt>@</tt>BuiltWith(methodName="identifiedBy")
 *         private Long id;
 *
 *         <tt>@</tt>BuiltWith(methodName="livingIn")
 *         private String zipCode;
 *
 *         <tt>@</tt>BuiltWith(methodName="withEmail")
 *         private String email;
 *
 *         protected User(){}
 *     }
 * </pre>
 * <p/>
 * Use the generated builder like this:
 * <pre>
 *     import static com.acme.UserBuilder.aUser;
 *     ...
 *     UserBuilder john = aUser().named("John").livingIn("94114").withEmail("johnDoe@acme.com").identifiedBy(1234L).build();
 *     doSomethingWith(john);
 * </pre>
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface BuiltWith {

    String USE_SENSIBLE_DEFAULT = "";

    /**
     * Specifies the name of the method that will be generated on the builder to assign values to the field.
     * <p>
     *     If blank, then "with" + the field name will be used.  For example, given private String name, the name
     *     of the buildable fluent method would be "withName()'.
     * </p>
     * @return The builder's fluent-api style assignment method name for this field.
     */
    String methodName() default USE_SENSIBLE_DEFAULT;

    /**
     * Specifies the default value the Builder will build the class with, as a string.
     * <p>
     *     The builder will apply the exact text in the string.
     * </p>
     * @return The default value in the builder, as a string.
     */
    String defaultValue() default USE_SENSIBLE_DEFAULT;

    String overrideArgType() default USE_SENSIBLE_DEFAULT;

    String overrideClassifer() default USE_SENSIBLE_DEFAULT;

    OverrideMethod overrideMethod() default OverrideMethod.NULL;

    enum OverrideMethod {
        NULL, AddToList
    }


}
