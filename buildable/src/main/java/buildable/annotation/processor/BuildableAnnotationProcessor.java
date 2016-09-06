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
import java.util.stream.Collectors;

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
        final Map<TypeElement, Buildable> buildables = roundEnvironment.getElementsAnnotatedWith(Buildable.class).stream().filter(v -> v.getKind().isClass()).map(v -> ((TypeElement) v)).collect(Collectors.toMap(t -> t, t -> t.getAnnotation(Buildable.class)));
        if (buildables.size() == 0) {
            return true;
        }

        final Map<TypeElement, List<VariableElement>> buildableFieldsMap= new HashMap<>();
        for (TypeElement eachBuildableTypeElement : buildables.keySet()) {
            buildableFieldsMap.put(eachBuildableTypeElement, new ArrayList<>());
            determineBuildableFields(eachBuildableTypeElement, eachBuildableTypeElement, buildableFieldsMap, roundEnvironment);
        }

        for (TypeElement eachBuildableTypeElement : buildables.keySet()) {

            Name simpleClassName = eachBuildableTypeElement.getSimpleName();
            Name qualifiedClassName = eachBuildableTypeElement.getQualifiedName();

            final Buildable theBuildable = buildables.get(eachBuildableTypeElement);
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
                            buildables
                    );
                }

                writer.writeBuildMethod(buildableFieldsMap.get(eachBuildableTypeElement));
                writer.finishClass(processingEnv.getFiler());

            } catch (Exception e) {
                this.processingEnv.getMessager().printMessage(
                        ERROR,
                        format("Error creating %s: %s",
                                builderName,
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

}
