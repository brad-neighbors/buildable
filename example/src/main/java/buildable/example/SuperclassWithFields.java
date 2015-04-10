/*
 * Project vCloud Air
 * Copyright (c) 2015 VMware, Inc. All rights reserved.
 * VMware Confidential.
 */
package buildable.example;

import buildable.annotation.BuildableSubclasses;

@BuildableSubclasses
public abstract class SuperclassWithFields {

    private String identifier;

    public String getIdentifier() {
        return identifier;
    }
}
