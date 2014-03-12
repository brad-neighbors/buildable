package buildable.example;

import buildable.annotation.Buildable;
import buildable.annotation.BuiltWith;

@Buildable(name = "FieldTestObjectBuilder", factoryMethod = "anObject")
public class FieldTestObject {

    @SuppressWarnings("UnusedDeclaration")
    @BuiltWith
    String stringWithNoSpecifiedMethodNameAndNoSpecifiedDefaultValue;

    @SuppressWarnings("UnusedDeclaration")
    @BuiltWith(defaultValue = "\"default value\"")
    String stringWithSpecifiedMethodNameButNoSpecifiedDefaultValue;

    @SuppressWarnings("UnusedDeclaration")
    @BuiltWith(methodName = "aString", defaultValue = "\"default value\"")
    String stringWithSpecifiedMethodNameAndSpecifiedDefaultValue;

    @SuppressWarnings("UnusedDeclaration")
    @BuiltWith
    Float floatObject;

    @SuppressWarnings("UnusedDeclaration")
    @BuiltWith
    float floatPrimitive;

    @SuppressWarnings("UnusedDeclaration")
    @BuiltWith
    Integer integerObject;

    @SuppressWarnings("UnusedDeclaration")
    @BuiltWith
    int integerPrimitive;

    @SuppressWarnings("UnusedDeclaration")
    @BuiltWith
    Double doubleObject;

    @SuppressWarnings("UnusedDeclaration")
    @BuiltWith
    double doublePrimitive;

    @SuppressWarnings("UnusedDeclaration")
    @BuiltWith
    Short shortObject;

    @SuppressWarnings("UnusedDeclaration")
    @BuiltWith
    short shortPrimitive;

    @SuppressWarnings("UnusedDeclaration")
    @BuiltWith
    Long longObject;

    @SuppressWarnings("UnusedDeclaration")
    @BuiltWith
    long longPrimitive;

    @SuppressWarnings("UnusedDeclaration")
    @BuiltWith
    Character charObject;

    @SuppressWarnings("UnusedDeclaration")
    @BuiltWith
    char charPrimitive;

    @SuppressWarnings("UnusedDeclaration")
    @BuiltWith
    Byte byteObject;

    @SuppressWarnings("UnusedDeclaration")
    @BuiltWith
    byte bytePrimitive;
}
