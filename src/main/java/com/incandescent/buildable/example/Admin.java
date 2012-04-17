package com.incandescent.buildable.example;

import com.incandescent.buildable.annotation.Buildable;
import com.incandescent.buildable.annotation.Fluently;

@Buildable(name = "AdminBuilder", makeAbstract = true)
public class Admin {

    @Fluently(methodName = "named", defaultValue = "\"suzieAdmin\"")
    private String name;

    protected Admin(){}

    public String getName() {
        return name;
    }
}
