package buildable.annotation.processor;

import buildable.annotation.Buildable;
import buildable.annotation.BuildableSubclasses;
import buildable.annotation.BuiltWith;
import buildable.annotation.ExcludeFromBuilder;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static buildable.annotation.processor.Util.createBuilderName;
import static java.lang.String.format;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;

/**
 * An annotation processor to generate fluent-api style builders for classes annotated with @Buildable, @BuildableSubclasses and @BuiltWith.
 */
@SupportedAnnotationTypes(value = {
        "buildable.annotation.BuildableSubclasses",
        "buildable.annotation.Buildable",
        "buildable.annotation.BuiltWith",
        "buildable.annotation.ExcludeFromBuilder"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SuppressWarnings("UnusedDeclaration")
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

        final Map<TypeElement, List<VariableElement>> buildableFieldsMap= new HashMap<>();
        for (Element eachBuildable : buildables) {
            TypeElement eachBuildableTypeElement = (TypeElement) eachBuildable;
            buildableFieldsMap.put(eachBuildableTypeElement, new ArrayList<VariableElement>());
            determineBuildableFields(eachBuildableTypeElement, eachBuildableTypeElement, buildableFieldsMap, roundEnvironment);
        }

        for (Element eachBuildableClass : buildables) {
            TypeElement eachBuildableTypeElement = (TypeElement) eachBuildableClass;

            Name simpleClassName = eachBuildableTypeElement.getSimpleName();
            Name qualifiedClassName = eachBuildableTypeElement.getQualifiedName();

            final Buildable theBuildable = eachBuildableTypeElement.getAnnotation(Buildable.class);
            final String builderName = createBuilderName(theBuildable, simpleClassName);

            try {

                final ClassFileWriter writer = new ClassFileWriter(theBuildable, qualifiedClassName);

                writer.writeClassDeclaration();
                writer.writeFactoryMethodAndConstructor();

                if (!theBuildable.cloneMethod().isEmpty()) {
                    writer.writeCloneableMethod(buildableFieldsMap.get(eachBuildableTypeElement));
                }

                for (VariableElement eachFieldToBuild : buildableFieldsMap.get(eachBuildableTypeElement)) {
                    final BuiltWith annotation = eachFieldToBuild.getAnnotation(BuiltWith.class);
                    final boolean hasBuiltWithSpecifications = annotation != null;

                    writer.writeFluentElement(
                            eachFieldToBuild,
                            annotation,
                            buildables,
                            hasBuiltWithSpecifications ?
                                    determineFieldDefaultValue(eachFieldToBuild, annotation, builderName) : "");
                }

                writer.writeBuildMethod(buildableFieldsMap.get(eachBuildableTypeElement));
                writer.finishClass(processingEnv.getFiler());

            } catch (Exception e) {
                this.processingEnv.getMessager().printMessage(
                        ERROR,
                        format("Error creating %s: %s",
                                qualifiedClassName.toString(),
                                e.toString()));
            }
        }
        return true;
    }

    private void determineBuildableFields(TypeElement buildable,
                                          TypeElement enclosingElement,
                                          Map<TypeElement, List<VariableElement>> buildableFieldsMap,
                                          RoundEnvironment roundEnvironment) {
        final List<? extends Element> enclosedElements = enclosingElement.getEnclosedElements();
        for (Element eachEnclosedElement : enclosedElements) {

            // exclude if not a field
            if (!eachEnclosedElement.getKind().isField()) {
                continue;
            }

            // exclude if explicitly annotated to be excluded
            final ExcludeFromBuilder shouldBeExcluded = eachEnclosedElement.getAnnotation(ExcludeFromBuilder.class);
            if (shouldBeExcluded != null) {
                continue;
            }

            // exclude if it's a static field
            if (eachEnclosedElement.getModifiers().contains(Modifier.STATIC)) {
                continue;
            }

            buildableFieldsMap.get(buildable).add((VariableElement) eachEnclosedElement);

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
                    determineBuildableFields(buildable, eachBuildableSubclassTypeElement, buildableFieldsMap,
                            roundEnvironment);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private String determineFieldDefaultValue(final VariableElement field,
                                              final BuiltWith builtWith,
                                              String builderName)
            throws ClassNotFoundException {

        String defaultValue = builtWith.defaultValue();
        if (BuiltWith.USE_SENSIBLE_DEFAULT.equals(defaultValue)) {
            try {
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
            }   catch(ClassNotFoundException e) {
                defaultValue = "null";
            }
        }
        this.processingEnv.getMessager().printMessage(NOTE,
                format("Determined default value for %s.%s: %s",
                        builderName,
                        field.getSimpleName(),
                        defaultValue));
        return defaultValue;
    }
}
