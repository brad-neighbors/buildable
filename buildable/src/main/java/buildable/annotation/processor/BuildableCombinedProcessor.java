package buildable.annotation.processor;

import buildable.annotation.Buildable;
import buildable.spec.BuildableSpec;
import buildable.spec.InjectBuildable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static buildable.annotation.processor.Util.defaultBuildable;
import static javax.tools.Diagnostic.Kind.NOTE;

/**
 * An annotation processor that will process both @Buildable and @BuildableSpec annotated classes.
 *
 * This processor also shares the classes that will have builders created for between the two annotation processors. This allows builder methods
 * that takes other builders to utilize builders created by the other annotation processor.
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class BuildableCombinedProcessor  extends AbstractProcessor {

    private BuildableAnnotationProcessor annotationProcessor;
    private BuildableSpecProcessor specProcessor;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        specProcessor = new BuildableSpecProcessor();
        annotationProcessor = new BuildableAnnotationProcessor();
        annotationProcessor.init(processingEnv);
        specProcessor.init(processingEnv);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        SupportedAnnotationTypes ann = BuildableAnnotationProcessor.class.getAnnotation(SupportedAnnotationTypes.class);
        SupportedAnnotationTypes spec = BuildableSpecProcessor.class.getAnnotation(SupportedAnnotationTypes.class);

        Set<String> supportedTypes = Arrays.stream(ann.value()).collect(Collectors.toSet());
        supportedTypes.addAll(Arrays.stream(spec.value()).collect(Collectors.toSet()));
        return Collections.unmodifiableSet(supportedTypes);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        this.processingEnv.getMessager().printMessage(NOTE, "Creating builders for classes annotated with @Buildable...");
        if (roundEnvironment.processingOver()) {
            return true;
        }
        Map<TypeElement, Buildable> allBuildables = getSpecBuildables(roundEnvironment);
        allBuildables.putAll(getAnnotationBuildables(roundEnvironment));

        //Allow the two annotation processors to know about builders created by the other.
        specProcessor.setAllBuildables(allBuildables);
        annotationProcessor.setAllBuildables(allBuildables);

        return annotationProcessor.process(annotations, roundEnvironment) && specProcessor.process(annotations, roundEnvironment);
    }

    private Map<TypeElement, Buildable> getAnnotationBuildables(RoundEnvironment roundEnvironment) {
        return roundEnvironment.getElementsAnnotatedWith(Buildable.class).stream().filter(v -> v.getKind().isClass()).map(v -> ((TypeElement) v)).collect(Collectors.toMap(t -> t, t -> t.getAnnotation(Buildable.class)));
    }

    private Map<TypeElement, Buildable> getSpecBuildables(RoundEnvironment roundEnvironment) {

        final Set<? extends Element> config = roundEnvironment.getElementsAnnotatedWith(BuildableSpec.class);
        Map<TypeElement, Buildable> buildables = new HashMap<>();
        for (Element element : config) {
            TypeElement configClass = (TypeElement) element;
            //Each field in the BuildableSpec class corresponds to a class we want to write a builder for.
            Set<VariableElement> buildableClasses = configClass.getEnclosedElements().stream().filter(c -> c.getKind().isField()).map(c -> ((VariableElement) c)).collect(Collectors.toSet());

            for (VariableElement buildableClass : buildableClasses) {
                DeclaredType typeMirror = (DeclaredType) buildableClass.asType();
                TypeElement clazz = (TypeElement) typeMirror.asElement();

                InjectBuildable injectBuildable = buildableClass.getAnnotation(InjectBuildable.class);

                buildables.put(clazz, injectBuildable == null ? defaultBuildable() : injectBuildable.value());
            }
        }
        return buildables;
    }
}
