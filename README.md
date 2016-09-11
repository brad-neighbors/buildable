# Buildable

A java annotation processor for creating object builders with a [fluent-interface](http://en.wikipedia.org/wiki/Fluent_interface).

* Annotate classes with `@Buildable`
* Annotate fields with `@BuiltWith`
* Support for subclasses `@BuildableSubclasses`
* Support for third party classes with `@BuildableSpec`, `@InjectBuildable` and `@BuildField`

# Pre-requisites

* Java 8, yup
* Gradle (to build from source)

# Releases via Maven Central

* [1.0.RELEASE](http://central.maven.org/maven2/com/bradneighbors/buildable/buildable/1.0.RELEASE/) on March 13, 2014

```xml
   <dependency>
    <groupId>com.bradneighbors.buildable</groupId>
    <artifactId>buildable</artifactId>
    <version>1.0.RELEASE</version>
   </dependency>
```

# Modules

## buildable

* Contains the `@Buildable`, `@BuildableSubclasses`, and `@BuiltWith` annotations
* Contains the `@BuildableSpec`, `@InjectBuildable`, and `@BuildField` annotations
* Contains the `Builder<T>` interface that all builders will implement
* Contains the annotation processors for creating Annotation Based Builders and Spec Based Builders.

## example

* Contains a few example classes with annotations
* Contains a few example classes using `@BuildableSpec`
* Contains unit tests that prove the annotation processor worked

# Gradle

To build:
<pre>
  gradle clean build
</pre>

After building, look at `example/generated/buildable/example/` to see what the generated builders look like.
You can understand their API by looking at the tests in `example/src/test/`

# Consuming in a Maven build

In your project's pom, you'll need something like this:

```xml
<dependencies>
    <dependency>
        <groupId>com.bradneighbors.buildable</groupId>
        <artifactId>buildable</artifactId>
        <version>1.0.RELEASE</version>
    </dependency>
</dependcies>
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.0</version>
            <configuration>
                <annotationProcessors>
                    <annotationProcessor>buildable.annotation.processor.BuildableAnnotationProcessor</annotationProcessor>
                </annotationProcessors>
                <debug>true</debug>
                <optimize>true</optimize>
                <source>1.7</source>
                <target>1.7</target>
                <compilerArguments>
                <AaddGeneratedAnnotation>true</AaddGeneratedAnnotation>
                <Adebug>true</Adebug>
                </compilerArguments>
            </configuration>
        </plugin>
    </plugins>
</build>
```

# Quick Example

For more examples, see the examples module.

To have a new builder generated for you given the following class:
```java
import java.util.Date;

public class User {

  private String name;
  private String info;
  private Date birthDate;

  public User(String name, String info, Date birthDate) {
    this.name = name;
    this.info = info;
    this.birthDate = birthDate;
  }
```

Simply add the following annotations:
```java
import java.util.Date;
import buildable.annotation.Buildable; // import this annotation
import buildable.annotation.BuiltWith; // import this annotation

@Buildable(name = "UserBuilder", factoryMethod = "aUser") 
public class User {

  @BuiltWith(methodName = "named", defaultValue = "Jane Doe")
  private String name;

  @BuiltWith
  private String info;

  @BuiltWith(methodName = "bornOn")
  private Date birthDate;

  private User(){}

  public User(String name, String info, Date birthDate) {
    this(); 
    this.name = name;
    this.info = info;
    this.birthDate = birthDate;
  }
}
```

This will generate a builder that looks something like this:
```java
import buildable.Builder;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;

public class UserBuilder implements Builder<User> {

  public static UserBuilder aUser() {
    return new UserBuilder();
  }

  private UserBuilder() {}

  private java.lang.String name = "Jane Doe";
  public UserBuilder named(java.lang.String name) {
    this.name = name;
    return this;
  }


  private java.lang.String info = null;
  public UserBuilder withInfo(java.lang.String info) {
    this.info = info;
	return this;
  }
	
  private java.util.Date birthDate = null;
  public UserBuilder bornOn(java.util.Date birthDate) {
    this.birthDate = birthDate;
    return this;
  }

  public User build() {
	// commenting out for brevity here, just try for yourself and you'll see the fields getting set...
  }
}
```

To do the same using `@BuildableSpec`, create a new file to specify the configuration
```java
import buildable.annotation.Buildable;
import buildable.annotation.BuiltWith;
import buildable.spec.BuildField;
import buildable.spec.BuildableSpec;
import buildable.spec.InjectBuildable;


@BuildableSpec
public class BuilderConfig {

    @InjectBuildable(value = @Buildable(factoryMethod = "aUser"),
            fields = {
                    @BuildField(name = "name", value =  @BuiltWith(methodName = "named", defaultValue = "\"Jane Doe\"") ),
                    @BuildField(name = "birthDate", value =   @BuiltWith(methodName = "bornOn")),
            }
    )
    private User user;
}
```

This will generate the exact same builder as the annotation based method.
	


