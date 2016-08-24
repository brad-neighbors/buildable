package buildable.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Specifies that the type is buildable using a Fluent-API style Builder pattern.</p>
 * <p>Classes annotated with @Buildable must have an empty protected (or public) constructor.</p>
 * <p>Each field that you would like the builder to use in a fluent api, annotate with @BuiltWith.</p>
 *
 * <p>The name of the Builder that is generated may be specified with the name().</p>
 * <p>The Builder's factory method may be specified with factoryMethod();</p>
 *
 * <p>Given a type User.java:</p>
 * <pre>
 *     package com.acme;
 *
 *     <code>@</code>Buildable(name="UserBuilder", factoryMethod="aUser")
 *     public class User {
 *
 *         <code>@</code>BuiltWith(methodName="named")
 *         private String name;
 *
 *         protected User(){}
 *     }
 * </pre>
 *
 * <p>Will result in a builder class: (with full implementation omitted for brevity..)</p>
 *
 * <pre>
 *  package com.acme;
 *  import buildable.Builder;
 *  ...
 *  public class UserBuilder implements Builder&lt;User&gt; {
 *
 *      public static UserBuilder aUser() {
 *          return new UserBuilder();
 *      }
 *      private UserBuilder(){}
 *
 *      public UserBuilder named(String name) {
 *          ...
 *      }
 *
 *      public User build() {
 *          ...
 *      }
 *  }
 * </pre>
 *
 *
 * <p>Conversely a builder can be specified to be built as an abstract class,
 * in which case the factoryMethod() will be ignored:</p>
 *
 * <pre>
 *     package com.acme;
 *
 *     <code>@</code>Buildable(name="UserBuilder", makeAbstract=true)
 *     public class User {
 *
 *         <code>@</code>BuiltWith(methodName="named")
 *         private String name;
 *
 *         protected User(){}
 *     }
 * </pre>
 *
 *
 * <p>Will result in a builder:</p>
 * <pre>
 *  package com.acme;
 *  import buildable.Builder;
 *  ...
 *  public abstract class UserBuilder implements Builder&lt;User&gt; {
 *
 *      protected UserBuilder(){}
 *
 *      public UserBuilder named(String name) {
 *          ...
 *      }
 *
 *      public User build() {
 *          ...
 *      }
 *  }
 * </pre>
 *
 * <p>That can of course be further subclassed as needed:</p>
 * <pre>
 *  package com.acme;
 *  import buildable.Builder;
 *  ...
 *  public class MyUserBuilder extends UserBuilder {
 *
 *       public static UserBuilder johnDoe() {
 *           return new UserBuilder().named("John Doe");
 *       }
 *  }
 * </pre>
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface Buildable {

    String USE_SENSIBLE_DEFAULT = "";


    /**
     * Specifies what the name of the Builder class will be.
     * @return The simple class name of the Builder class.
     */
    String name() default USE_SENSIBLE_DEFAULT;

    /**
     * Specifies if the generated builder class is to be abstract or not.
     * @return <code>true</code> if the builder should be abstract.
     */
    boolean makeAbstract() default false;

    /**
     * Specifies what the Builder's static factory method will be named.
     * @return The builder's sole factory method.
     */
    String factoryMethod() default USE_SENSIBLE_DEFAULT;

    String cloneMethod() default USE_SENSIBLE_DEFAULT;
}
