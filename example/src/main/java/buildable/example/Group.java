/* **********************************************************************
 * Copyright 2016 VMware, Inc.  All rights reserved. VMware Confidential
 * *********************************************************************/

package buildable.example;

import java.util.List;

import buildable.annotation.Buildable;
import buildable.annotation.BuiltWith;

/**
 * An example POJO that will help demonstrate the @BuiltWith annotations with add an element to a list.
 */
@Buildable
public class Group {

    private String name;
    @BuiltWith(overrideMethod = BuiltWith.OverrideMethod.AddToList, overrideClassifer = "java.util.LinkedList")
    private List<User> users;

    public String getName() {
        return name;
    }

    public List<User> getUsers() {
        return users;
    }
}
