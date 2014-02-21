package com.incandescent.buildable.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that the type is buildable using a Fluent-API style Builder pattern.
 * <p/>
 * Classes annotated with @Buildable must have an empty protected (or public) constructor.
 * <p/>
 * Each field that you would like the builder to use in a fluent api, annotate with @Fluently.
 * <p/>
 * The name of the Builder that is generated may be specified with the name().
 * <p/>
 * The Builder's factory method may be specified with factoryMethod();
 * <p/>
 * Given a type User.java:
 * <pre>
 *     package com.acme;
 *
 *     <tt>@</tt>Buildable(name="UserBuilder", factoryMethod="aUser")
 *     public class User {
 *
 *         <tt>@</tt>Fluently(methodName="named")
 *         private String name;
 *
 *         protected User(){}
 *     }
 * </pre>
 * <p/>
 * Will result in a builder class: (with full implementation omitted for brevity..)
 *
 * <pre>
 *  package com.acme;
 *  import com.incandescent.buildable.Builder;
 *  ...
 *  public class UserBuilder implements Builder<tt><</tt>User<tt>></tt> {
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
 * <p/>
 * Conversely a builder can be specified to be built as an abstract class,
 * in which case the factoryMethod() will be ignored:
 *
 * <pre>
 *     package com.acme;
 *
 *     <tt>@</tt>Buildable(name="UserBuilder", makeAbstract=true)
 *     public class User {
 *
 *         <tt>@</tt>Fluently(methodName="named")
 *         private String name;
 *
 *         protected User(){}
 *     }
 * </pre>
 * <p/>
 *
 * Will result in a builder:
 * <pre>
 *  package com.acme;
 *  import com.incandescent.buildable.Builder;
 *  ...
 *  public abstract class UserBuilder implements Builder<tt><</tt>User<tt>></tt> {
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
 * <p/>
 * That can of course be further subclassed as needed:
 * <pre>
 *   package com.acme;
 *  import com.incandescent.buildable.Builder;
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

    public static final String USE_SENSIBLE_DEFAULT = "";


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
