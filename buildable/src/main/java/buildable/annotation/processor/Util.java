/*
 * Project vCloud Air
 * Copyright (c) 2015 VMware, Inc. All rights reserved.
 * VMware Confidential.
 */
package buildable.annotation.processor;

import buildable.annotation.Buildable;

import javax.lang.model.element.Name;

/**
 * Shared utilities.
 */
public class Util {

    public static String packageNameOf(final Name qualifiedClassName) {
        final int indexOfLastPeriod = qualifiedClassName.toString().lastIndexOf(".");
        return qualifiedClassName.toString().substring(0, indexOfLastPeriod);
    }

    public static String capitalize(final Name simpleName) {
        final String name = simpleName.toString();
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

}
