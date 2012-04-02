/* **************************************************************************
 * Copyright (C) 2012 VMware, Inc. All rights reserved.
 * -- VMware Confidential
 * **************************************************************************/
package com.incandescent.buildable.example;

import com.incandescent.buildable.Fluently;

/**
 * An example abstract class that has an @Fluently annotation marked on it to test if @Buildable subclasses will have
 * these fields included in their builders.
 */
public abstract class Identified {

    @Fluently(methodName = "identifiedBy")
    private String id;

    protected Identified(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
