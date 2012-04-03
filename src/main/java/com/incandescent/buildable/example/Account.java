package com.incandescent.buildable.example;

import com.incandescent.buildable.annotation.Buildable;
import com.incandescent.buildable.annotation.Fluently;

/**
 * An example POJO that will help demonstrate the @Fluently annotations work when included in superclasses.
 */
@Buildable(name = "AccountBuilder", factoryMethod = "anAccount")
public class Account extends Identified {

    private static final String EMPTY_ACCOUNT_ID = "unassigned";

    @Fluently(methodName = "named")
    private String name;


    public Account(String id){
        super(id);
    }

    protected Account(){
        super(EMPTY_ACCOUNT_ID);
    }

    public String getName() {
        return name;
    }
}
