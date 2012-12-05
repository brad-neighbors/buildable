/* **************************************************************************
 * Copyright (C) 2012 VMware, Inc. All rights reserved.
 * -- VMware Confidential
 * **************************************************************************/
package com.incandescent.buildable.example;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AbstractBuilderTest {

    private static class TestingExampleAdminBuilder extends AdminBuilder {
        public static AdminBuilder adminJohn() {
            return new TestingExampleAdminBuilder().named("John");
        }
        public static AdminBuilder adminJane() {
            return new TestingExampleAdminBuilder().named("Jane");
        }
    }

    @Test public void canExtendAbstractBuilders() {
        assertEquals("Name field matches", "John", TestingExampleAdminBuilder.adminJohn().build().getName());
        assertEquals("Can still chain", "Foobar", TestingExampleAdminBuilder.adminJane().named("Foobar").build().getName());
    }
}
