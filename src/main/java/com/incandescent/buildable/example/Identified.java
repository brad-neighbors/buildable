package com.incandescent.buildable.example;

import com.incandescent.buildable.BuildableSubclasses;
import com.incandescent.buildable.Fluently;

/**
 * An example abstract class that has an @Fluently annotation marked on it to test if @Buildable subclasses will have
 * these fields included in their builders.
 */
@BuildableSubclasses
public abstract class Identified {

    @Fluently(methodName = "identifiedBy", defaultValue = "\"id_123\"")
    private String id;

    protected Identified(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
