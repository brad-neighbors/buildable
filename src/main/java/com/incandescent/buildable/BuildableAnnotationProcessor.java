package com.incandescent.buildable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
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
 * An annotation processor to generate fluent-api style builders for classes annotated with @Buildable and @Fluently.
 */
@SupportedAnnotationTypes(value = {"*"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class BuildableAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> typeElements, RoundEnvironment roundEnvironment) {
        if (roundEnvironment.processingOver()) {
            return true;
        }
        final Set<? extends Element> buildables = roundEnvironment.getElementsAnnotatedWith(Buildable.class);
        if (buildables.size() == 0) {
            return true;
        }
        final Set<? extends Element> fluentlies = roundEnvironment.getElementsAnnotatedWith(Fluently.class);

        final Map<TypeElement, List<VariableElement>> buildableToFluentlyMap = new HashMap<TypeElement, List<VariableElement>>();
        for (Element eachBuildable : buildables) {
            TypeElement eachBuildableTypeElement = (TypeElement) eachBuildable;
            buildableToFluentlyMap.put(eachBuildableTypeElement, new ArrayList<VariableElement>());
        }

        // map each @Fluently to its enclosing @Buildable
        for (Element eachFluently : fluentlies) {
            final Name classNameWhereFluentlyExists = eachFluently.getEnclosingElement().getSimpleName();
            for (Element eachBuildable : buildables) {
                TypeElement eachBuildableTypeElement = (TypeElement) eachBuildable;
                if (eachBuildable.getSimpleName().equals(classNameWhereFluentlyExists)) {
                    VariableElement eachFluentlyVariableElement = (VariableElement) eachFluently;
                    buildableToFluentlyMap.get(eachBuildableTypeElement).add(eachFluentlyVariableElement);
                }
            }
        }

        for (Element eachBuildableClass : buildables) {
            TypeElement eachBuildableTypeElement = (TypeElement) eachBuildableClass;

            Name simpleClassName = eachBuildableTypeElement.getSimpleName();
            Name qualifiedClassName = eachBuildableTypeElement.getQualifiedName();

            try {
                final Buildable theBuildable = eachBuildableTypeElement.getAnnotation(Buildable.class);
                final JavaFileObject javaFileObject = processingEnv.getFiler().createSourceFile(qualifiedClassName + "Builder", eachBuildableClass);

                final OutputStream outputStream = javaFileObject.openOutputStream();
                final OutputStreamWriter out = new OutputStreamWriter(outputStream);

                writePackageAndImports(qualifiedClassName, out);

                writeClassDeclaration(simpleClassName, theBuildable, out);

                writeFactoryMethodAndConstructor(theBuildable, out);

                for (VariableElement eachFluently : buildableToFluentlyMap.get(eachBuildableTypeElement)) {
                    writeFluentElement(eachFluently, theBuildable.name(), out);
                }

                writeBuildMethod(buildableToFluentlyMap, eachBuildableTypeElement, simpleClassName, out);

                line("}", out);

                out.flush();
                outputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private void writeBuildMethod(Map<TypeElement, List<VariableElement>> buildableToFluentlyMap, TypeElement eachBuildableTypeElement, Name simpleClassName, OutputStreamWriter out) throws IOException {
        line(format("\tpublic %s build() {", simpleClassName), out);
        line("\t\ttry {", out);
        line(format("\t\t\tfinal Class clazz = Class.forName(%s.class.getCanonicalName());", simpleClassName.toString()), out);
        line(format("\t\t\tfinal %s instance = (%s) clazz.newInstance();", simpleClassName.toString(), simpleClassName.toString()), out);
        emptyLine(out);

        for (VariableElement eachFluently : buildableToFluentlyMap.get(eachBuildableTypeElement)) {
            line(format("\t\t\tfinal Field %sField = clazz.getDeclaredField(\"%s\");", eachFluently.getSimpleName(), eachFluently.getSimpleName()), out);
            line(format("\t\t\t%sField.setAccessible(true);", eachFluently.getSimpleName()), out);
            line(format("\t\t\t%sField.set(instance, %s);", eachFluently.getSimpleName(), eachFluently.getSimpleName()), out);
            line(format("\t\t\t%sField.setAccessible(false);", eachFluently.getSimpleName()), out);
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

    private void writeFactoryMethodAndConstructor(Buildable theBuildable, OutputStreamWriter out) throws IOException {
        line(format("\tpublic static %s %s() {",
          theBuildable.name(),
          theBuildable.factoryMethod()),
          out);

        line(format("\t\treturn new %s();",
          theBuildable.name()),
          out);

        line("\t}", out);
        emptyLine(out);

        line(format("\tprivate %s() {}",
          theBuildable.name()),
          out);

        emptyLine(out);
    }

    private void writeClassDeclaration(Name simpleName, Buildable theBuildable, OutputStreamWriter out) throws IOException {
        line(format("public class %s implements Builder<%s> {",
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

        emptyLine(out);
        emptyLine(out);
    }

    private void writeFluentElement(VariableElement variableElement, String builderName, OutputStreamWriter out) throws IOException {
        final Fluently annotation = variableElement.getAnnotation(Fluently.class);
        line(format("\tprivate %s %s = %s;",
          variableElement.asType(),
          variableElement.getSimpleName().toString(),
          annotation.defaultValue()),
          out);

        line(format("\tpublic %s %s(%s %s) {",
          builderName, annotation.methodName(),
          variableElement.asType(),
          variableElement.getSimpleName()),
          out);

        line(format("\t\tthis.%s = %s;",
          variableElement.getSimpleName(),
          variableElement.getSimpleName()),
          out);

        line(format("\t\treturn this;"), out);
        line("\t}", out);

        emptyLine(out);
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
