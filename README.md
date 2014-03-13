# Buildable

A java annotation processor for creating object builders with a fluent-api style.

* Annotate classes with @Buildable
* Annotate fields with @BuiltWith
* Support for subclasses @BuildableSubclasses

# Pre-requisites

* Java 7
* Gradle (to build from source)

# Releases [Maven Central](http://central.maven.org/maven2/com/bradneighbors/buildable/)

* 1.0.RELEASE on March 13, 2014

<pre>
   <dependency>
    <groupId>com.bradneighbors.buildable</groupId>
    <artifactId>buildable</artifactId>
    <version>1.0.RELEASE</version>
   </dependency>
</pre>

# Modules

## buildable

* Contains the *@Buildable*, *@BuildableSubclasses*, and *@BuiltWith* annotations
* Contains the *Builder<T>* interface that all builders will implement
* Contains the annotation processor

## example

* Contains a few example classes with annotations
* Contains unit tests that prove the annotation processor worked

# Gradle

To build:
<pre>
  gradle clean build
</pre>

