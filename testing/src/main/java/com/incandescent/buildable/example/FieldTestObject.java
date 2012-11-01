/*******************************************************************************
 * Copyright (C) 2012 VMware, Inc. All rights reserved.
 * -- VMware Confidential
 ******************************************************************************/
package com.incandescent.buildable.example;

import com.incandescent.buildable.annotation.Buildable;
import com.incandescent.buildable.annotation.BuiltWith;

@Buildable(name = "FieldTestObjectBuilder", factoryMethod = "anObject")
public class FieldTestObject {

    @BuiltWith
    String stringWithNoSpecifiedMethodNameAndNoSpecifiedDefaultValue;

    @BuiltWith(defaultValue = "\"default value\"")
    String stringWithSpecifiedMethodNameButNoSpecifiedDefaultValue;

    @BuiltWith(methodName = "aString", defaultValue = "\"default value\"")
    String stringWithSpecifiedMethodNameAndSpecifiedDefaultValue;

    @BuiltWith
    Float floatObject;

    @BuiltWith
    float floatPrimitive;

    @BuiltWith
    Integer integerObject;

    @BuiltWith
    int integerPrimitive;

    @BuiltWith
    Double doubleObject;

    @BuiltWith
    double doublePrimitive;

    @BuiltWith
    Short shortObject;

    @BuiltWith
    short shortPrimitive;

    @BuiltWith
    Long longObject;

    @BuiltWith
    long longPrimitive;

    @BuiltWith
    Character charObject;

    @BuiltWith
    char charPrimitive;

    @BuiltWith
    Byte byteObject;

    @BuiltWith
    byte bytePrimitive;
}
