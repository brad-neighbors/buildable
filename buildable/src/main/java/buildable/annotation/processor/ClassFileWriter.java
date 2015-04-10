/*
 * Project vCloud Air
 * Copyright (c) 2015 VMware, Inc. All rights reserved.
 * VMware Confidential.
 */
package buildable.annotation.processor;

import buildable.annotation.Buildable;
import buildable.annotation.BuiltWith;

import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.JavaFileObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Set;

import static buildable.annotation.processor.Util.capitalize;
import static buildable.annotation.processor.Util.createBuilderName;
import static buildable.annotation.processor.Util.packageNameOf;
import static java.lang.String.format;

/**
 * Responsible for writing Builder class files.
 */
public class ClassFileWriter {

    private final Writer out;
    private final Buildable theBuildable;
    private final OutputStream outputStream;

    public ClassFileWriter(Buildable theBuildable, JavaFileObject javaFileObject) throws IOException {
        this.theBuildable = theBuildable;
        this.outputStream = javaFileObject.openOutputStream();
        this.out = new OutputStreamWriter(outputStream);
    }

    public void writePackageAndImports(Name qualifiedName) throws IOException {
        line("package " + packageNameFromQualifiedName(qualifiedName) + ";");
        emptyLine();
        line("import buildable.Builder;");
        line("import java.lang.reflect.Field;");
        line("import java.lang.reflect.Method;");
        emptyLine();
        emptyLine();
    }


    public void writeClassDeclaration(Name simpleName) throws IOException {
        line(format("public %s class %s implements Builder<%s> {",
                theBuildable.makeAbstract() ? "abstract" : "",
                createBuilderName(theBuildable, simpleName),
                simpleName));

        emptyLine();
    }

    public void writeFactoryMethodAndConstructor(Name simpleName) throws IOException {
        // honor the "factoryMethod" name in the @Buildable if not building an abstract clas
        if (!theBuildable.makeAbstract()) {
            line(format("\tpublic static %s %s() {",
                            createBuilderName(theBuildable, simpleName),
                            createFactoryMethodName(theBuildable, simpleName)));

            line(format("\t\treturn new %s();", createBuilderName(theBuildable, simpleName)));

            line("\t}");
            emptyLine();
        }

        // if it's abstract, make the constructor protected, private otherwise
        if (theBuildable.makeAbstract()) {
            line(format("\tprotected %s() {}",
                            createBuilderName(theBuildable, simpleName)));
        } else {
            line(format("\tprivate %s() {}",
                            createBuilderName(theBuildable, simpleName)));
        }

        emptyLine();
    }

    public void writeFluentElement(VariableElement field,
                                   String builderName,
                                   final Set<? extends Element> buildables,
                                   String defaultValue) throws Exception{

        final BuiltWith annotation = field.getAnnotation(BuiltWith.class);
        final boolean hasBuiltWithSpecifications = annotation != null;

        // write the field declaration
        if (field.asType().getKind().isPrimitive() || !hasBuiltWithSpecifications) {
            // it's primitive, so let's not assign a default value...
            line(format("\tprivate %s %s;",
                            field.asType(),
                            field.getSimpleName().toString()));
        } else {
            if (defaultValue.isEmpty()) {
                line(format("\tprivate %s %s;",
                                field.asType(),
                                field.getSimpleName().toString()));
            } else {
                line(format("\tprivate %s %s = %s;",
                                field.asType(),
                                field.getSimpleName().toString(),
                                defaultValue));
            }
        }

        String methodName = determineFluentMethodName(annotation, field);

        if (!hasBuiltWithSpecifications || BuiltWith.USE_SENSIBLE_DEFAULT.equals(annotation.overrideArgType())){
            // write the fluent built-with method that takes in the instance of the field
            line(format("\tpublic %s %s(%s %s) {",
                            builderName,
                            methodName,
                            field.asType(),
                            field.getSimpleName()));
        } else {
            line(format("\tpublic %s %s(%s %s) {",
                    builderName,
                    methodName,
                    annotation.overrideArgType(),
                    field.getSimpleName()));
        }

        if (hasBuiltWithSpecifications && annotation.overrideMethod() != BuiltWith.OverrideMethod.NULL) {
            switch (annotation.overrideMethod()) {
                case AddToList:
                    line(format("\t\tthis.%s = new %s;", 
                            field.getSimpleName(), 
                            annotation.overrideClassifer()));
                    line(format("\t\tjava.util.Collections.addAll(this.%s, %s);", 
                                    field.getSimpleName(),
                                    field.getSimpleName()));
            }

        } else {
            line(format("\t\tthis.%s = %s;",
                            field.getSimpleName(),
                            field.getSimpleName()));
        }

        line("\t\treturn this;");
        line("\t}");

        emptyLine();

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

            final String packageNameOVariableBuilder = packageNameOf(((TypeElement) variableClassElement)
                    .getQualifiedName());
            final Name classNameOfVariableBuilder = variableClassElement.getSimpleName();
            final Buildable variableBuildable = variableClassElement.getAnnotation(Buildable.class);

            line(format("\tpublic %s %s(%s %s) {", 
                    builderName, 
                    methodName,
                    packageNameOVariableBuilder + "." + createBuilderName(variableBuildable, classNameOfVariableBuilder),
                    field.getSimpleName() + "Builder"));

            line(format("\t\tthis.%s = %s.build();",
                            field.getSimpleName(),
                            field.getSimpleName() + "Builder"));

            line("\t\treturn this;");
            line("\t}");
        }

        emptyLine();
    }

    public void writeBuildMethod(
            List<VariableElement> fieldsToBuild,
            Name simpleClassName) throws IOException {
        
        line(format("\tpublic %s build() {", simpleClassName));
        line("\t\ttry {");
        line(format("\t\t\tfinal Class clazz = Class.forName(%s.class.getCanonicalName());", simpleClassName.toString()));
        line(format("\t\t\tfinal %s instance = (%s) clazz.newInstance();", simpleClassName.toString(), simpleClassName.toString()));
        emptyLine();

        for (VariableElement eachField : fieldsToBuild) {

            line("\t\t\ttry {");
            line(format("\t\t\t\tfinal Method %sMethod = clazz.getDeclaredMethod(\"set%s\", %s.class);",
                    eachField.getSimpleName(), capitalize(eachField.getSimpleName()),
                    eachField.asType().toString().replaceAll("<[.,<>a-zA-Z0-9]*>", "")));
            line(format("\t\t\t\t%sMethod.setAccessible(true);", eachField.getSimpleName()));
            line(format("\t\t\t\t%sMethod.invoke(instance, %s);", eachField.getSimpleName(),
                    eachField.getSimpleName()));
            line("\t\t\t} catch (NoSuchMethodException nsme) {");
            line("\t\t\t\t// method doesn't exist, set field directly");

            line(format("\t\t\t\tfinal Field %sField = getDeclaredField(clazz, \"%s\");",
                    eachField.getSimpleName(), eachField.getSimpleName()));
            line(format("\t\t\t\t%sField.setAccessible(true);", eachField.getSimpleName()));
            line(format("\t\t\t\t%sField.set(instance, %s);", eachField.getSimpleName(),
                    eachField.getSimpleName()));
            line(format("\t\t\t\t%sField.setAccessible(false);", eachField.getSimpleName()));
            line("\t\t\t}");
            emptyLine();
        }

        line("\t\t\treturn instance;");

        line("\t\t} catch (Exception e) {");
        line("\t\t\te.printStackTrace();");
        line("\t\t} catch (Error e) {");
        line("\t\t\te.printStackTrace();");
        line("\t\t}");

        line("\t\treturn null;");
        line("\t}");
    }


    public void finishClass() throws IOException {
        line("\tprivate Field getDeclaredField(Class clazz, String fieldName) throws NoSuchFieldException {");
        line("\t\ttry {");
        line("\t\t\treturn clazz.getDeclaredField(fieldName);");
        line("\t\t} catch (NoSuchFieldException e) {");
        line("\t\t\tfinal Class superclass = clazz.getSuperclass();");
        line("\t\t\tif (superclass == null) {");
        line("\t\t\t\tthrow e;");
        line("\t\t\t} else {");
        line("\t\t\t\treturn getDeclaredField(superclass, fieldName);");
        line("\t\t\t}");
        line("\t\t}");
        line("\t}");
        line("}");
        
        out.flush();
        outputStream.close();
    }

    public void writeCloneableMethod(Name simpleName, List<VariableElement> elements) {
        try {
            char variable = simpleName.toString().toLowerCase().charAt(0);
            line(format("\tpublic %s %s (%s %c) {",
                    createBuilderName(theBuildable, simpleName),
                    theBuildable.cloneMethod(),
                    simpleName,
                    variable));
            for (VariableElement eachFluently : elements) {
                line(format("\t\tthis.%s = %c.get%s();",
                        eachFluently.getSimpleName(),
                        variable,
                        capitalize(eachFluently.getSimpleName())));
            }

            line(format("\t\treturn new %s();", createBuilderName(theBuildable, simpleName)));

            line("\t}");
            emptyLine();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String determineFluentMethodName(final BuiltWith annotation, final VariableElement field) {
        if (annotation != null && !BuiltWith.USE_SENSIBLE_DEFAULT.equals(annotation.methodName())) {
            return annotation.methodName();
        }
        return "with" + capitalize(field.getSimpleName());
    }

    private String createFactoryMethodName(Buildable buildable, Name className) {
        if (buildable.factoryMethod().equals(Buildable.USE_SENSIBLE_DEFAULT)) {
            if (className.toString().matches("[AEIOUaeiou].*")) {
                return "an" + className.toString();
            } else {
                return "a" + className.toString();
            }
        } else {
            return buildable.factoryMethod();
        }
    }

    private String packageNameFromQualifiedName(Name qualifiedName) {
        String fullClassName = qualifiedName.toString();
        final int lastDot = fullClassName.lastIndexOf(".");
        if (lastDot > 0) {
            return fullClassName.substring(0, lastDot);
        }
        return "";
    }

    private void emptyLine() throws IOException {
        out.write('\n');
    }

    private void line(String text) throws IOException {
        out.write(text);
        out.write('\n');
    }
}
