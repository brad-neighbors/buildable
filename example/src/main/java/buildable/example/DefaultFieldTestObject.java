/*
 * Project vCloud Air
 * Copyright (c) 2015 VMware, Inc. All rights reserved.
 * VMware Confidential.
 */
package buildable.example;

import buildable.annotation.Buildable;
import buildable.annotation.ExcludeFromBuilder;

@Buildable
public class DefaultFieldTestObject extends SuperclassWithFields {

    @SuppressWarnings("UnusedDeclaration")
    @ExcludeFromBuilder
    String fieldToNotBeBuiltWith;

    @SuppressWarnings("UnusedDeclaration")
    Float floatObject;

    @SuppressWarnings("UnusedDeclaration")
    float floatPrimitive;

    @SuppressWarnings("UnusedDeclaration")
    Integer integerObject;

    @SuppressWarnings("UnusedDeclaration")
    int integerPrimitive;

    @SuppressWarnings("UnusedDeclaration")
    Double doubleObject;

    @SuppressWarnings("UnusedDeclaration")
    double doublePrimitive;

    @SuppressWarnings("UnusedDeclaration")
    Short shortObject;

    @SuppressWarnings("UnusedDeclaration")
    short shortPrimitive;

    @SuppressWarnings("UnusedDeclaration")
    Long longObject;

    @SuppressWarnings("UnusedDeclaration")
    long longPrimitive;

    @SuppressWarnings("UnusedDeclaration")
    Character charObject;

    @SuppressWarnings("UnusedDeclaration")
    char charPrimitive;

    @SuppressWarnings("UnusedDeclaration")
    Byte byteObject;

    @SuppressWarnings("UnusedDeclaration")
    byte bytePrimitive;
}
