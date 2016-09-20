/*
 * Project vCloud Air
 * Copyright (c) 2015 VMware, Inc. All rights reserved.
 * VMware Confidential.
 */
package buildable.annotation.processor;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

import buildable.spec.BuildConstructor;
import buildable.spec.ConstructorArg;
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
import static java.util.Arrays.asList;

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

    public void writeFluentElement(ConstructorArg arg, Map<TypeElement, Buildable> buildables) throws Exception {
        BuiltWith annotation = arg.value();
        TypeName className;
        TypeMirror fieldType = null;
        try {
            className = ClassName.get(arg.type());
        } catch (MirroredTypeException mte) {
            fieldType = mte.getTypeMirror();
            className = ClassName.get(fieldType);
        }

        String fieldName = arg.name();
        String methodName = determineFluentMethodName(annotation, fieldName);

        writeField(fieldName, annotation, className);
        writeSetter(fieldName, fieldType, annotation, true, className, methodName);
        writeMethodForFieldBuilderIfExists(fieldName, fieldType, buildables, methodName);
    }

    public void writeFluentElement(VariableElement field, BuiltWith annotation, Map<TypeElement, Buildable> buildables) throws Exception {

        final boolean hasBuiltWithSpecifications = annotation != null;

        TypeName fieldClassName = TypeName.get(field.asType());
        String fieldName = field.getSimpleName().toString();
        String methodName = determineFluentMethodName(annotation, field.getSimpleName().toString());
        TypeMirror fieldType = field.asType();

        // write the field declaration
        writeField(fieldName, annotation, fieldClassName);
        writeSetter(fieldName, fieldType, annotation, hasBuiltWithSpecifications, fieldClassName, methodName);
        writeMethodForFieldBuilderIfExists(fieldName, fieldType, buildables, methodName);
    }

    private void writeSetter(String fieldName, TypeMirror fieldType, BuiltWith annotation, boolean hasBuiltWithSpecifications, TypeName fieldClassName, String methodName) {
        MethodSpec.Builder fieldMethod = MethodSpec.methodBuilder(methodName).addModifiers(Modifier.PUBLIC).returns(builderClass);

        if (hasBuiltWithSpecifications && annotation.overrideMethod() == BuiltWith.OverrideMethod.AddToList) {
            TypeName innerClass = ((ParameterizedTypeName) ParameterizedTypeName.get(fieldType)).typeArguments.get(0);
            fieldMethod.addParameter(ArrayTypeName.of(innerClass), fieldName);

            TypeName listImpl;
            if (BuiltWith.USE_SENSIBLE_DEFAULT.equals(annotation.overrideClassifer())) {
                listImpl = ParameterizedTypeName.get(ClassName.get(ArrayList.class), ((ParameterizedTypeName) ParameterizedTypeName.get(fieldType)).typeArguments.get(0));
            } else {
                listImpl = ClassName.get(packageNameFromQualifiedName(annotation.overrideClassifer()), classNameFromQualifiedName(annotation.overrideClassifer()));
            }
            fieldMethod.addStatement("this.$L = new $T()", fieldName, listImpl);
            fieldMethod.addStatement("$T.addAll(this.$L, $L)", Collections.class, fieldName, fieldName);
            fieldMethod.varargs();

        } else {
            // write the fluent built-with method that takes in the instance of the field
            fieldMethod.addParameter(fieldClassName, fieldName);
            fieldMethod.addStatement("this.$L = $L", fieldName, fieldName);
        }

        fieldMethod.addStatement("return this");
        builder.addMethod(fieldMethod.build());
    }

    private void writeField(String fieldName, BuiltWith annotation, TypeName fieldClassName) {
        FieldSpec.Builder fieldBuilder = FieldSpec.builder(fieldClassName, fieldName)
            .addModifiers(Modifier.PRIVATE);

        if (annotation != null) {
            if (!annotation.defaultValue().equals(BuiltWith.USE_SENSIBLE_DEFAULT)) {
                //If the Class of the field is String use a string substitution otherwise use a literal.
                String sub = "java.lang.String".equals(fieldClassName.toString()) ? "$S" : "$L";
                fieldBuilder.initializer(sub, annotation.defaultValue());
            }
        }

        builder.addField(fieldBuilder.build());
    }

    /**
     * check each @Buildable, if the field itself is of a class marked @Buildable, we can overload
     * the fluent built-with method to also accept its builder as a parameter
     */
    private void writeMethodForFieldBuilderIfExists(String fieldName, TypeMirror fieldType, Map<TypeElement, Buildable> buildables, String methodName) {
        Optional<TypeElement> buildableVariable = buildables.keySet().stream().filter(eachBuildable -> eachBuildable.asType().equals(fieldType)).findFirst();

        if (buildableVariable.isPresent()) {
            TypeElement variableClassElement = buildableVariable.get();
            final String packageNameOVariableBuilder = packageNameOf(variableClassElement.getQualifiedName());
            final Name classNameOfVariableBuilder = variableClassElement.getSimpleName();
            final Buildable variableBuildable = buildables.get(variableClassElement);

            ClassName fieldBuildableClass = ClassName.get(packageNameOVariableBuilder, createBuilderName(variableBuildable, classNameOfVariableBuilder));

            MethodSpec builderMethod = MethodSpec.methodBuilder(methodName).addModifiers(Modifier.PUBLIC)
                    .returns(builderClass)
                    .addParameter(fieldBuildableClass, fieldName + "Builder")
                    .addStatement("this.$L = $L.build()", fieldName, fieldName + "Builder")
                    .addStatement("return this").build();

            builder.addMethod(builderMethod);
        }
    }

    public void writeBuildMethod(List<VariableElement> fieldsToBuild) throws IOException {
        writeBuildMethod(fieldsToBuild, new ArrayList<>());
    }

    public void writeBuildMethod(List<VariableElement> fieldsToBuild, List<ConstructorArg> args) throws IOException {
        MethodSpec.Builder buildMethod = MethodSpec.methodBuilder("build")
                .addAnnotation(ClassName.get(Override.class))
                .returns(builtClass)
                .addModifiers(Modifier.PUBLIC)
                .beginControlFlow("try")
                .addStatement("final $T clazz = $T.forName($T.class.getCanonicalName())", Class.class, Class.class, builtClass);

        if (args == null || args.isEmpty()) {
            buildMethod.addStatement("final $T instance = ($T) clazz.newInstance()", builtClass, builtClass);
        } else {
            TypeName[] constructorArgTypes = args.stream().map(Util::extractTypeName).toArray(TypeName[]::new);
            String[] constructorArgs = args.stream().map(ConstructorArg::name).toArray(String[]::new);

            StringBuilder getConstructor = new StringBuilder("final $T<?> cons = clazz.getConstructor($T.class");
            StringBuilder invoke= new StringBuilder("final $T instance = ($T) cons.newInstance($L");
            for (int i = 0; i< args.size()-1; i++) {
                getConstructor.append(",$T.class");
                invoke.append(",$L");
            }

            List paramList = new ArrayList();
            paramList.add(Constructor.class);
            paramList.addAll(asList(constructorArgTypes));
            buildMethod.addStatement(getConstructor.append(")").toString(), paramList.toArray());

            paramList.clear();
            paramList.add(builtClass);
            paramList.add(builtClass);
            paramList.addAll(asList(constructorArgs));
            buildMethod.addStatement(invoke.append(")").toString(), paramList.toArray());
        }

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

    private String determineFluentMethodName(final BuiltWith annotation, final String fieldName) {
        if (annotation != null && !BuiltWith.USE_SENSIBLE_DEFAULT.equals(annotation.methodName())) {
            return annotation.methodName();
        }
        return "with" + capitalize(fieldName);
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
