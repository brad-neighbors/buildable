package com.incandescent.buildable.processor;

import com.incandescent.buildable.annotation.Buildable;
import com.incandescent.buildable.annotation.BuildableSubclasses;
import com.incandescent.buildable.annotation.BuiltWith;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import static javax.tools.Diagnostic.Kind.*;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;

/**
 * An annotation processor to generate fluent-api style builders for classes annotated with @Buildable, @BuildableSubclasses and @BuiltWith.
 */
@SupportedAnnotationTypes(value = {
  "com.incandescent.buildable.annotation.BuildableSubclasses",
  "com.incandescent.buildable.annotation.Buildable",
  "com.incandescent.buildable.annotation.Fluently"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class BuildableAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> allTypeElements, RoundEnvironment roundEnvironment) {
        this.processingEnv.getMessager().printMessage(NOTE, "Creating builders for classes annotated with @Buildable...");
        if (roundEnvironment.processingOver()) {
            return true;
        }
        final Set<? extends Element> buildables = roundEnvironment.getElementsAnnotatedWith(Buildable.class);
        if (buildables.size() == 0) {
            return true;
        }

        final Map<TypeElement, List<VariableElement>> buildableToFluentlyMap = new HashMap<TypeElement, List<VariableElement>>();
        for (Element eachBuildable : buildables) {
            TypeElement eachBuildableTypeElement = (TypeElement) eachBuildable;
            buildableToFluentlyMap.put(eachBuildableTypeElement, new ArrayList<VariableElement>());

            addEachFluentlyEnclosedElement(eachBuildableTypeElement, eachBuildableTypeElement, buildableToFluentlyMap,
              roundEnvironment);
        }


        for (Element eachBuildableClass : buildables) {
            TypeElement eachBuildableTypeElement = (TypeElement) eachBuildableClass;

            Name simpleClassName = eachBuildableTypeElement.getSimpleName();
            Name qualifiedClassName = eachBuildableTypeElement.getQualifiedName();
            String packageName = getPackageNameFrom(qualifiedClassName);

            try {
                final Buildable theBuildable = eachBuildableTypeElement.getAnnotation(Buildable.class);
                final JavaFileObject javaFileObject = processingEnv.getFiler().createSourceFile(packageName + "." +
                        theBuildable.name(), eachBuildableClass);

                final OutputStream outputStream = javaFileObject.openOutputStream();
                final OutputStreamWriter out = new OutputStreamWriter(outputStream);

                writePackageAndImports(qualifiedClassName, out);

                writeClassDeclaration(simpleClassName, theBuildable, out);

                writeFactoryMethodAndConstructor(theBuildable, out);

                for (VariableElement eachFluently : buildableToFluentlyMap.get(eachBuildableTypeElement)) {
                    writeFluentElement(eachFluently, theBuildable.name(), out, buildables);
                }

                writeBuildMethod(buildableToFluentlyMap, eachBuildableTypeElement, simpleClassName, out);

                writeDeclaredFieldFinder(out);

                line("}", out);

                out.flush();
                outputStream.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private String getPackageNameFrom(final Name qualifiedClassName) {
        final int indexOfLastPeriod = qualifiedClassName.toString().lastIndexOf(".");
        return qualifiedClassName.toString().substring(0, indexOfLastPeriod);
    }

    private void addEachFluentlyEnclosedElement(TypeElement buildable,
                                                TypeElement enclosingElement,
                                                Map<TypeElement, List<VariableElement>> buildableToFluentlyMap,
                                                RoundEnvironment roundEnvironment) {
        final List<? extends Element> enclosedElements = enclosingElement.getEnclosedElements();
        for (Element eachEnclosedElement : enclosedElements) {
            if (eachEnclosedElement.getKind().isField()) {
                final BuiltWith annotation = eachEnclosedElement.getAnnotation(BuiltWith.class);
                if (annotation != null) {
                    buildableToFluentlyMap.get(buildable).add((VariableElement) eachEnclosedElement);
                }
            }
        }

        final String superclassName = enclosingElement.getSuperclass().toString();
        this.processingEnv.getMessager().printMessage(NOTE, "Beginning superclass processing for " + superclassName);

        final Set<? extends Element> buildableSubclasses = roundEnvironment.getElementsAnnotatedWith(BuildableSubclasses.class);

        for (Element eachBuildableSubclassElement : buildableSubclasses) {
            TypeElement eachBuildableSubclassTypeElement = (TypeElement) eachBuildableSubclassElement;
            final String eachBuildableSubclassClassName = eachBuildableSubclassTypeElement.getQualifiedName().toString();
            this.processingEnv.getMessager().printMessage(NOTE, "Checking " + eachBuildableSubclassClassName);

            if (eachBuildableSubclassTypeElement.getKind().isClass()) {

                this.processingEnv.getMessager().printMessage(NOTE, "Checking " + superclassName + " equals " + eachBuildableSubclassClassName);
                if (superclassName.equals(eachBuildableSubclassClassName)) {
                    addEachFluentlyEnclosedElement(buildable, eachBuildableSubclassTypeElement, buildableToFluentlyMap, roundEnvironment);
                }
            }
        }
    }

    private void writeBuildMethod(Map<TypeElement, List<VariableElement>> buildableToFluentlyMap, TypeElement eachBuildableTypeElement, Name simpleClassName, OutputStreamWriter out) throws IOException {
        line(format("\tpublic %s build() {", simpleClassName), out);
        line("\t\ttry {", out);
        line(format("\t\t\tfinal Class clazz = Class.forName(%s.class.getCanonicalName());", simpleClassName.toString()), out);
        line(format("\t\t\tfinal %s instance = (%s) clazz.newInstance();", simpleClassName.toString(), simpleClassName.toString()), out);
        emptyLine(out);

        for (VariableElement eachFluently : buildableToFluentlyMap.get(eachBuildableTypeElement)) {

            line("\t\t\ttry {", out);
            line(format("\t\t\t\tfinal Method %sMethod = clazz.getDeclaredMethod(\"set%s\", %s.class);",
                    eachFluently.getSimpleName(), capitalize(eachFluently.getSimpleName()),
                    eachFluently.asType()), out);
            line(format("\t\t\t\t%sMethod.setAccessible(true);", eachFluently.getSimpleName()), out);
            line(format("\t\t\t\t%sMethod.invoke(instance, %s);", eachFluently.getSimpleName(),
                    eachFluently.getSimpleName()), out);
            line("\t\t\t} catch (NoSuchMethodException nsme) {", out);
            line("\t\t\t\t// method doesn't exist, set field directly", out);

            line(format("\t\t\t\tfinal Field %sField = getDeclaredField(clazz, \"%s\");",
                    eachFluently.getSimpleName(), eachFluently.getSimpleName()), out);
            line(format("\t\t\t\t%sField.setAccessible(true);", eachFluently.getSimpleName()), out);
            line(format("\t\t\t\t%sField.set(instance, %s);", eachFluently.getSimpleName(),
                    eachFluently.getSimpleName()), out);
            line(format("\t\t\t\t%sField.setAccessible(false);", eachFluently.getSimpleName()), out);
            line("\t\t\t}", out);
            emptyLine(out);
        }

        line("\t\t\treturn instance;", out);

        line("\t\t} catch (Exception e) {", out);
        line("\t\t\te.printStackTrace();", out);
        line("\t\t} catch (Error e) {", out);
        line("\t\t\te.printStackTrace();", out);
        line("\t\t}", out);

        line("\t\treturn null;", out);
        line("\t}", out);
    }

    private Object capitalize(final Name simpleName) {
        final String name = simpleName.toString();
        return name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
    }


    private void writeDeclaredFieldFinder(OutputStreamWriter out) throws IOException {
        line("\tprivate Field getDeclaredField(Class clazz, String fieldName) throws NoSuchFieldException {", out);
        line("\t\ttry {", out);
        line("\t\t\treturn clazz.getDeclaredField(fieldName);", out);
        line("\t\t} catch (NoSuchFieldException e) {", out);
        line("\t\t\tfinal Class superclass = clazz.getSuperclass();", out);
        line("\t\t\tif (superclass == null) {", out);
        line("\t\t\t\tthrow e;", out);
        line("\t\t\t} else {", out);
        line("\t\t\t\treturn getDeclaredField(superclass, fieldName);", out);
        line("\t\t\t}", out);
        line("\t\t}", out);
        line("\t}", out);
    }

    private void writeFactoryMethodAndConstructor(Buildable theBuildable, OutputStreamWriter out) throws IOException {
        // honor the "factoryMethod" name in the @Buildable if not building an abstract clas
        if (!theBuildable.makeAbstract()) {
            line(format("\tpublic static %s %s() {",
              theBuildable.name(),
              theBuildable.factoryMethod()),
              out);

            line(format("\t\treturn new %s();",
              theBuildable.name()),
              out);

            line("\t}", out);
            emptyLine(out);
        }

        // if it's abstract, make the constructor protected, private otherwise
        if (theBuildable.makeAbstract()) {
            line(format("\tprotected %s() {}",
                      theBuildable.name()),
                      out);
        } else {
            line(format("\tprivate %s() {}",
              theBuildable.name()),
              out);
        }

        emptyLine(out);
    }

    private void writeClassDeclaration(Name simpleName, Buildable theBuildable, OutputStreamWriter out) throws IOException {
        line(format("public %s class %s implements Builder<%s> {",
          theBuildable.makeAbstract() ? "abstract" : "",
          theBuildable.name(),
          simpleName)
          , out);

        emptyLine(out);
    }

    private void writePackageAndImports(Name qualifiedName, OutputStreamWriter out) throws IOException {
        line("package " + packageNameFromQualifiedName(qualifiedName) + ";", out);
        emptyLine(out);
        line("import com.incandescent.buildable.Builder;", out);
        line("import java.lang.reflect.Field;", out);
        line("import java.lang.reflect.Method;", out);
        emptyLine(out);
        emptyLine(out);
    }

    private void writeFluentElement(VariableElement field, String builderName, OutputStreamWriter out,
                                    final Set<? extends Element> buildables) throws Exception{

        final BuiltWith annotation = field.getAnnotation(BuiltWith.class);

        // determine the default value


        // write the field declaration
        if (field.asType().getKind().isPrimitive()) {
            // it's primitive, so let's not assign a default value...
            line(format("\tprivate %s %s;",
              field.asType(),
              field.getSimpleName().toString()),
              out);
        } else {
            String defaultValue = determineDefaultValue(field, annotation);
            line(format("\tprivate %s %s = %s;",
              field.asType(),
              field.getSimpleName().toString(),
              defaultValue),
              out);
        }

        String methodName = determineFluentMethodName(annotation, field);

        // write the fluent built-with method that takes in the instance of the field
        line(format("\tpublic %s %s(%s %s) {",
          builderName, methodName,
          field.asType(),
          field.getSimpleName()),
          out);

        line(format("\t\tthis.%s = %s;",
          field.getSimpleName(),
          field.getSimpleName()),
          out);

        line(format("\t\treturn this;"), out);
        line("\t}", out);

        emptyLine(out);

        // check each @Buildable, if the field itself is of a class marked @Buildable, we can overload
        // the fluent built-with method to also accept its builder as a parameter
        boolean foundBuilderForVariable = false;
        Element variableClassElement = null;
        for (Element eachBuildable : buildables) {
            if (eachBuildable.asType().equals(field.asType())) {
                foundBuilderForVariable = true;
                variableClassElement = eachBuildable;
                break;
            }
        }

        if (foundBuilderForVariable) {

            final String packageNameOVariableBuilder = getPackageNameFrom(((TypeElement) variableClassElement)
                    .getQualifiedName());
            final Buildable variableBuildable = variableClassElement.getAnnotation(Buildable.class);

            line(format("\tpublic %s %s(%s %s) {", builderName, annotation.methodName(),
                    packageNameOVariableBuilder + "." + variableBuildable.name(),
                    field.getSimpleName() + "Builder"), out);

            line(format("\t\tthis.%s = %s.build();",
                      field.getSimpleName(),
                      field.getSimpleName() + "Builder"),
                      out);

            line(format("\t\treturn this;"), out);
            line("\t}", out);
        }

        emptyLine(out);
    }

    private String determineFluentMethodName(final BuiltWith annotation, final VariableElement field) {
        if (!BuiltWith.USE_SENSIBLE_DEFAULT.equals(annotation.methodName())) {
            return annotation.methodName();
        }
        return "with" + capitalize(field.getSimpleName());
    }

    private String determineDefaultValue(final VariableElement field, final BuiltWith builtWith)
            throws ClassNotFoundException {

        System.err.println("Determining default value...");
        String defaultValue = builtWith.defaultValue();

        if (BuiltWith.USE_SENSIBLE_DEFAULT.equals(defaultValue)) {
            if (!field.asType().getKind().isPrimitive()) {
                Class clazz = Class.forName(field.asType().toString());
                if (clazz.isAssignableFrom(String.class)) {
                    defaultValue = "\"value\"";
                } else if (clazz.isAssignableFrom(Character.class)) {
                    defaultValue = "\'\\u0000\'";
                } else if (clazz.isAssignableFrom(Float.class)) {
                    defaultValue = "0f";
                } else if (clazz.isAssignableFrom(Integer.class)) {
                    defaultValue = "0";
                } else if (clazz.isAssignableFrom(Short.class)) {
                    defaultValue = "0";
                } else if (clazz.isAssignableFrom(Long.class)) {
                    defaultValue = "0L";
                } else if (clazz.isAssignableFrom(Double.class)) {
                    defaultValue = "0D";
                } else if (clazz.isAssignableFrom(Boolean.class)) {
                    defaultValue = "false";
                } else if (clazz.isAssignableFrom(Byte.class)) {
                    defaultValue = "Byte.MIN_VALUE";
                } else {
                    defaultValue = "null";
                }
            }
        }
        return defaultValue;
    }

    private String packageNameFromQualifiedName(Name qualifiedName) {
        String fullClassName = qualifiedName.toString();
        final int lastDot = fullClassName.lastIndexOf(".");
        if (lastDot > 0) {
            return fullClassName.substring(0, lastDot);
        }
        return "";
    }

    private void emptyLine(OutputStreamWriter writer) throws IOException {
        writer.write('\n');
    }

    private void line(String text, OutputStreamWriter writer) throws IOException {
        writer.write(text);
        writer.write('\n');
    }
}
