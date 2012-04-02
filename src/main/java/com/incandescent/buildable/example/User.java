package com.incandescent.buildable.example;

import com.incandescent.buildable.Buildable;
import com.incandescent.buildable.Fluently;

/**
 * An example POJO that demonstrates generating a UserBuilder.
 */
@Buildable(name = "UserBuilder", factoryMethod = "aUser")
public class User {

    @Fluently(methodName = "named", defaultValue = "\"John Doe\"")
    private String name;

    @Fluently(methodName = "withEmail", defaultValue = "\"johnDoe@acme.com\"")
    private String email;

    @Fluently(methodName = "withSsn")
    private String ssn;

    @Fluently(methodName = "livingInZip", defaultValue = "94114")
    private Integer zipCode;

    protected User(){}

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getSsn() {
        return ssn;
    }

    public Integer getZipCode() {
        return zipCode;
    }
}
