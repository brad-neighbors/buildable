package buildable.spec.processor;

import buildable.annotation.Buildable;
import buildable.annotation.BuiltWith;
import buildable.annotation.processor.ClassFileWriter;
import buildable.spec.BuildField;
import buildable.spec.BuildableSpec;
import buildable.spec.InjectBuildable;
import com.squareup.javapoet.ClassName;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;

@SupportedAnnotationTypes(value = {
        "buildable.spec.BuildableSpec",
        "buildable.spec.InjectBuildable",
        "buildable.spec.BuildField"
})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SuppressWarnings("UnusedDeclaration")
public class BuildableSpecProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        this.processingEnv.getMessager().printMessage(NOTE, "Creating builders for classes annotated with @Buildable...");
        if (roundEnvironment.processingOver()) {
            return true;
        }
        final Set<? extends Element> config = roundEnvironment.getElementsAnnotatedWith(BuildableSpec.class);
        if (config.size() == 0) {
            return true;
        }

        Elements elementUtils = processingEnv.getElementUtils();
        for (Element element : config) {
            TypeElement configClass = (TypeElement) element;
            //Each field in the BuildableSpec class corresponds to a class we want to write a builder for.
            Set<VariableElement> buildableClasses = configClass.getEnclosedElements().stream().filter(c -> c.getKind().isField()).map(c -> ((VariableElement) c)).collect(Collectors.toSet());

            //Build a map of Classes (TypeElements) to Buildables
            Map<TypeElement, Buildable> buildables = new HashMap<>();

            for (VariableElement buildableClass : buildableClasses) {
                DeclaredType typeMirror = (DeclaredType) buildableClass.asType();
                TypeElement clazz = (TypeElement) typeMirror.asElement();

                InjectBuildable injectBuildable = buildableClass.getAnnotation(InjectBuildable.class);

                buildables.put(clazz, injectBuildable == null ? defaultBuildable() : injectBuildable.value());
            }

            //Each field in the builder config
            for (VariableElement fieldClass : buildableClasses) {

                //Change to field to type element
                DeclaredType typeMirror = (DeclaredType) fieldClass.asType();
                TypeElement classToBuild = (TypeElement) typeMirror.asElement();

                //fields of the buildable class

                ClassName className = ClassName.get(classToBuild);
                ClassName builderName = ClassName.get(className.packageName(), className.simpleName() + "Builder");

                InjectBuildable injectBuildable = fieldClass.getAnnotation(InjectBuildable.class);

                if (injectBuildable == null) {
                    injectBuildable = defaultInjectBuildable();
                }

                //Fields to build
                Map<String, VariableElement> fields = determineFieldsToBuild(classToBuild, asList(injectBuildable.excludedFields()));

                //Fields that have BuiltWith added via @BuildField
                Map<String, BuiltWith> fieldBuilders = Arrays.stream(injectBuildable.fields()).collect(Collectors.toMap(BuildField::name, BuildField::value));

                // Class injected buildable
                Buildable theBuildable = injectBuildable.value();

                try {
                    ClassFileWriter classWriter = new ClassFileWriter(theBuildable, classToBuild.getQualifiedName());
                    classWriter.writeClassDeclaration();
                    classWriter.writeFactoryMethodAndConstructor();

                    if (!theBuildable.cloneMethod().isEmpty()) {
                        classWriter.writeCloneableMethod(new ArrayList<>(fields.values()));
                    }

                    for (String fieldName : fields.keySet()) {
                        VariableElement field = fields.get(fieldName);
                        BuiltWith builtWith = fieldBuilders.get(fieldName);
                        boolean hasBuiltWith = builtWith != null;

                        classWriter.writeFluentElement(field, builtWith, buildables);
                    }

                    classWriter.writeBuildMethod(new ArrayList<>(fields.values()));
                    classWriter.finishClass(processingEnv.getFiler());

                } catch (Exception e) {
                    this.processingEnv.getMessager().printMessage(
                            ERROR,
                            format("Error creating %s: %s",
                                    builderName.toString(),
                                    e.toString()));
                }
            }
        }


        return true;
    }

    private Map<String, VariableElement> determineFieldsToBuild(TypeElement clazz, List<String> excludedFields) {
        Map<String, VariableElement> fields = clazz.getEnclosedElements().stream().filter(v -> v.getKind().isField()).map(v -> ((VariableElement) v)).collect(Collectors.toMap(f -> f.getSimpleName().toString(), f -> f));
        fields.entrySet().removeIf(e -> excludedFields.contains(e.getKey()));

        if (clazz.asType().getKind().isPrimitive()) {
            return fields;
        } else if (clazz.getSuperclass().getKind().equals(TypeKind.NONE)) {
            return fields;
        } else {
            DeclaredType superclass = (DeclaredType) clazz.getSuperclass();
            fields.putAll(determineFieldsToBuild(((TypeElement) superclass.asElement()), excludedFields));
            return fields;
        }
    }

    private InjectBuildable defaultInjectBuildable() {
        return new InjectBuildable() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return InjectBuildable.class;
            }

            @Override
            public String[] excludedFields() {
                return new String[0];
            }

            @Override
            public Buildable value() {
                return defaultBuildable();
            }

            @Override
            public BuildField[] fields() {
                return new BuildField[0];
            }
        };
    }

    private Buildable defaultBuildable() {
        return new Buildable() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return Buildable.class;
            }

            @Override
            public String name() {
                return Buildable.USE_SENSIBLE_DEFAULT;
            }

            @Override
            public boolean makeAbstract() {
                return false;
            }

            @Override
            public String factoryMethod() {
                return Buildable.USE_SENSIBLE_DEFAULT;
            }

            @Override
            public String cloneMethod() {
                return Buildable.USE_SENSIBLE_DEFAULT;
            }
        };
    }

}
