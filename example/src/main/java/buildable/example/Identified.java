package buildable.example;

import buildable.annotation.BuildableSubclasses;
import buildable.annotation.BuiltWith;

/**
 * An example abstract class that has an @BuiltWith annotation marked on it to test if @Buildable subclasses will have
 * these fields included in their builders.
 */
@BuildableSubclasses
public abstract class Identified {

    @BuiltWith(methodName = "identifiedBy", defaultValue = "\"id_123\"")
    private String id;

    protected Identified(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
