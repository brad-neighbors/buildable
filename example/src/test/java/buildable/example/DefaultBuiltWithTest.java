/*
 * Project vCloud Air
 * Copyright (c) 2015 VMware, Inc. All rights reserved.
 * VMware Confidential.
 */
package buildable.example;

import org.junit.Test;

import static buildable.example.DefaultFieldTestObjectBuilder.aDefaultFieldTestObject;
import static org.junit.Assert.assertEquals;

public class DefaultBuiltWithTest {

    @Test
    public void needNotSpecifyBuiltWithToGetAFieldBuilderMethod() {
        String id = "id";
        DefaultFieldTestObject obj = aDefaultFieldTestObject().withIdentifier(id).build();
        assertEquals(id, obj.getIdentifier());
    }
}
