/*
 * Project vCloud Air
 * Copyright (c) 2015 VMware, Inc. All rights reserved.
 * VMware Confidential.
 */
package buildable.annotation.processor;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import buildable.Builder;
import buildable.annotation.Buildable;
import buildable.annotation.BuiltWith;

import static buildable.annotation.processor.Util.capitalize;
import static buildable.annotation.processor.Util.createBuilderName;
import static buildable.annotation.processor.Util.packageNameOf;

/**
 * Responsible for writing Builder class files.
 */
public class ClassFileWriter {

    private static final MethodSpec GET_DECLARED_FIELD_METHOD = createGetDeclaredFieldMethod();
    private final Buildable theBuildable;
    private TypeSpec.Builder builder;
    private ClassName builderClass;
    private ClassName builtClass;
    private String packageName;

    public ClassFileWriter(Buildable theBuildable, Name qualifiedClassName) throws IOException {
        this.theBuildable = theBuildable;
        this.packageName = packageNameFromQualifiedName(qualifiedClassName);
        this.builtClass = ClassName.get(packageName, classNameFromQualifiedName(qualifiedClassName));
        this.builderClass = ClassName.get(packageName, createBuilderName(theBuildable, classNameFromQualifiedName(qualifiedClassName)));
    }


    public void writeClassDeclaration() throws IOException {
        ParameterizedTypeName typeName = ParameterizedTypeName.get(ClassName.get(Builder.class), builtClass);
        builder = TypeSpec.classBuilder(builderClass).addModifiers(Modifier.PUBLIC).addSuperinterface(typeName);

        if (theBuildable.makeAbstract()) {
            builder.addModifiers(Modifier.ABSTRACT);
        }
    }

    public void writeFactoryMethodAndConstructor() throws IOException {
        // honor the "factoryMethod" name in the @Buildable if not building an abstract clas
        if (!theBuildable.makeAbstract()) {
            MethodSpec factoryMethod = MethodSpec.methodBuilder(createFactoryMethodName(theBuildable, builtClass.simpleName()))
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(builderClass)
                    .addStatement("return new $T()", builderClass).build();
            builder.addMethod(factoryMethod);
        }

        // if it's abstract, make the constructor protected, private otherwise
        builder.addMethod(MethodSpec
                .constructorBuilder()
                .addModifiers(theBuildable.makeAbstract() ? Modifier.PROTECTED : Modifier.PRIVATE)
                .build());
    }

    public void writeFluentElement(VariableElement field,
            BuiltWith annotation,
            final Set<? extends Element> buildables,
            String defaultValue) throws Exception {

        final boolean hasBuiltWithSpecifications = annotation != null;

        // write the field declaration
        FieldSpec.Builder fieldBuilder = FieldSpec.builder(TypeName.get(field.asType()), field.getSimpleName().toString())
                .addModifiers(Modifier.PRIVATE);
        if (!defaultValue.isEmpty()) {
            fieldBuilder.initializer(defaultValue);
        }
        builder.addField(fieldBuilder.build());


        String methodName = determineFluentMethodName(annotation, field);
        MethodSpec.Builder fieldMethod = MethodSpec.methodBuilder(methodName).addModifiers(Modifier.PUBLIC).returns(builderClass);

        if (hasBuiltWithSpecifications && annotation.overrideMethod() == BuiltWith.OverrideMethod.AddToList) {
            TypeName innerClass = ((ParameterizedTypeName) ParameterizedTypeName.get(field.asType())).typeArguments.get(0);
            fieldMethod.addParameter(ArrayTypeName.of(innerClass), field.getSimpleName().toString());

            TypeName listImpl;
            if (BuiltWith.USE_SENSIBLE_DEFAULT.equals(annotation.overrideClassifer())) {
                listImpl = ParameterizedTypeName.get(ClassName.get(ArrayList.class), ((ParameterizedTypeName) ParameterizedTypeName.get(field.asType())).typeArguments.get(0));
            } else {
                listImpl = ClassName.get(packageNameFromQualifiedName(annotation.overrideClassifer()), classNameFromQualifiedName(annotation.overrideClassifer()));
            }
            fieldMethod.addStatement("this.$L = new $T()", field.getSimpleName(), listImpl);
            fieldMethod.addStatement("$T.addAll(this.$L, $L)", Collections.class, field.getSimpleName(), field.getSimpleName());
            fieldMethod.varargs();

        } else {
            // write the fluent built-with method that takes in the instance of the field
            fieldMethod.addParameter(TypeName.get(field.asType()), field.getSimpleName().toString());
            fieldMethod.addStatement("this.$L = $L", field.getSimpleName(), field.getSimpleName());
        }

        fieldMethod.addStatement("return this");
        builder.addMethod(fieldMethod.build());

        writeMethodForFieldBuilderIfExists(field, buildables, methodName);
    }

    /**
     * check each @Buildable, if the field itself is of a class marked @Buildable, we can overload
     * the fluent built-with method to also accept its builder as a parameter
     */
    private void writeMethodForFieldBuilderIfExists(VariableElement field, Set<? extends Element> buildables,
            String methodName) {
        Optional<TypeElement> buildableVariable = buildables.stream().map(e -> ((TypeElement) e)).filter(eachBuildable -> eachBuildable.asType().equals
                (field.asType())).findFirst();

        if (buildableVariable.isPresent()) {
            TypeElement variableClassElement = buildableVariable.get();
            final String packageNameOVariableBuilder = packageNameOf(variableClassElement.getQualifiedName());
            final Name classNameOfVariableBuilder = variableClassElement.getSimpleName();
            final Buildable variableBuildable = variableClassElement.getAnnotation(Buildable.class);

            ClassName fieldBuildableClass = ClassName.get(packageNameOVariableBuilder, createBuilderName
                    (variableBuildable, classNameOfVariableBuilder));

            MethodSpec builderMethod = MethodSpec.methodBuilder(methodName).addModifiers(Modifier.PUBLIC)
                    .returns(builderClass)
                    .addParameter(fieldBuildableClass, field.getSimpleName() + "Builder")
                    .addStatement("this.$L = $L.build()", field.getSimpleName(), field.getSimpleName() + "Builder")
                    .addStatement("return this").build();

            builder.addMethod(builderMethod);
        }
    }

    public void writeBuildMethod(List<VariableElement> fieldsToBuild) throws IOException {
        MethodSpec.Builder buildMethod = MethodSpec.methodBuilder("build")
                .addAnnotation(ClassName.get(Override.class))
                .returns(builtClass)
                .addModifiers(Modifier.PUBLIC)
                .beginControlFlow("try")
                .addStatement("final $T clazz = $T.forName($T.class.getCanonicalName())", Class.class, Class.class, builtClass)
                .addStatement("final $T instance = ($T) clazz.newInstance()", builtClass, builtClass);

        for (VariableElement eachField : fieldsToBuild) {
            String methodName = eachField.getSimpleName() + "Method";
            String fieldName = eachField.getSimpleName() + "Field";

            buildMethod.beginControlFlow("try")
                    .addStatement("final $T $L = clazz.getDeclaredMethod($S, $L.class)", Method.class, methodName,
                            "set" + capitalize(eachField.getSimpleName()), eachField.asType().toString().replaceAll
                                    ("<[.,<>a-zA-Z0-9]*>", ""))
                    .addStatement("$L.setAccessible(true)", methodName)
                    .addStatement("$L.invoke(instance, $L)", methodName, eachField.getSimpleName())
                    .nextControlFlow("catch ($T nsme)", NoSuchMethodException.class)
                    .addStatement("final $T $L = $N(clazz, $S)", Field.class, fieldName, GET_DECLARED_FIELD_METHOD,
                            eachField.getSimpleName().toString())
                    .addStatement("$L.setAccessible(true)", fieldName)
                    .addStatement("$L.set(instance, $L)", fieldName, eachField.getSimpleName())
                    .addStatement("$L.setAccessible(false)", fieldName)
                    .endControlFlow();

        }

        buildMethod.addStatement("return instance")
                .nextControlFlow("catch ($T | $T e)", Exception.class, Error.class)
                .addStatement("e.printStackTrace()")
                .endControlFlow()
                .addStatement("return null");


        builder.addMethod(buildMethod.build());
    }


    public void finishClass(Filer filer) throws IOException {
        builder.addMethod(GET_DECLARED_FIELD_METHOD);
        JavaFile javaFile = JavaFile.builder(packageName, builder.build()).indent("\t").build();

        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static MethodSpec createGetDeclaredFieldMethod() {
        ParameterSpec classParam = ParameterSpec.builder(Class.class, "clazz").build();
        ParameterSpec fieldParam = ParameterSpec.builder(String.class, "fieldName").build();
        return MethodSpec.methodBuilder("getDeclaredField")
                .addException(NoSuchFieldException.class)
                .addModifiers(Modifier.PRIVATE)
                .returns(Field.class)
                .addParameter(classParam).addParameter(fieldParam)
                .beginControlFlow("try")
                .addStatement("return $N.getDeclaredField($N)", classParam, fieldParam)
                .nextControlFlow("catch ($T e)", NoSuchFieldException.class)
                .addStatement("Class superclass = $N.getSuperclass();", classParam)
                .beginControlFlow("if (superclass == null)")
                .addStatement("throw e")
                .nextControlFlow("else")
                .addStatement("return getDeclaredField(superclass, $N)", fieldParam)
                .endControlFlow()
                .endControlFlow()
                .build();
    }

    public void writeCloneableMethod(List<VariableElement> elements) {
        MethodSpec.Builder clone = MethodSpec.methodBuilder(theBuildable.cloneMethod()).addModifiers(Modifier.PUBLIC)
                .returns(builderClass)
                .addParameter(builtClass, "original");

        for (VariableElement eachFluently : elements) {
            clone.addStatement("this.$L = $L.get$L()", eachFluently.getSimpleName(), "original", capitalize(eachFluently.getSimpleName()));
        }
        clone.addStatement("return this");
        builder.addMethod(clone.build());

    }

    private String determineFluentMethodName(final BuiltWith annotation, final VariableElement field) {
        if (annotation != null && !BuiltWith.USE_SENSIBLE_DEFAULT.equals(annotation.methodName())) {
            return annotation.methodName();
        }
        return "with" + capitalize(field.getSimpleName());
    }

    private String createFactoryMethodName(Buildable buildable, String className) {
        if (buildable.factoryMethod().equals(Buildable.USE_SENSIBLE_DEFAULT)) {
            if (className.matches("[AEIOUaeiou].*")) {
                return "an" + className;
            } else {
                return "a" + className;
            }
        } else {
            return buildable.factoryMethod();
        }
    }

    private String packageNameFromQualifiedName(Name qualifiedName) {
        return packageNameFromQualifiedName(qualifiedName.toString());
    }

    private String packageNameFromQualifiedName(String fullClassName) {
        final int lastDot = fullClassName.lastIndexOf(".");
        if (lastDot > 0) {
            return fullClassName.substring(0, lastDot);
        }
        return "";
    }

    private String classNameFromQualifiedName(Name fullClassName) {
        return classNameFromQualifiedName(fullClassName.toString());
    }

    private String classNameFromQualifiedName(String fullClassName) {
        final int lastDot = fullClassName.lastIndexOf(".");
        if (lastDot > 0 && lastDot < fullClassName.length() - 1) {
            return fullClassName.substring(lastDot+1);
        }
        return "";
    }
}
