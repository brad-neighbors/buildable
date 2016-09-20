/*
 * Project vCloud Air
 * Copyright (c) 2015 VMware, Inc. All rights reserved.
 * VMware Confidential.
 */
package buildable.annotation.processor;

import buildable.annotation.Buildable;
import buildable.spec.ConstructorArg;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Name;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

import java.lang.annotation.Annotation;

/**
 * Shared utilities.
 */
public class Util {

    public static String packageNameOf(final Name qualifiedClassName) {
        final int indexOfLastPeriod = qualifiedClassName.toString().lastIndexOf(".");
        return qualifiedClassName.toString().substring(0, indexOfLastPeriod);
    }

    public static String capitalize(final Name simpleName) {
        return capitalize(simpleName.toString());
    }

    public static String capitalize(final String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
    }

    public static String createBuilderName(Buildable buildable, Name className) {
        return createBuilderName(buildable, className.toString());
    }

    public static String createBuilderName(Buildable buildable, String className) {
        if (buildable.name().equals(Buildable.USE_SENSIBLE_DEFAULT)) {
            return className + "Builder";
        } else {
            return buildable.name();
        }
    }

    public static TypeName extractTypeName(ConstructorArg arg) {
        try {
            return ClassName.get(arg.type());
        } catch (MirroredTypeException mte) {
            TypeMirror typeMirror = mte.getTypeMirror();
            return ClassName.get(typeMirror);
        }
    }

    public static Buildable defaultBuildable() {
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
