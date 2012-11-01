package com.incandescent.buildable.example;

import com.incandescent.buildable.annotation.Buildable;
import com.incandescent.buildable.annotation.BuiltWith;

/**
 * An example POJO that demonstrates generating a UserBuilder.
 */
@Buildable(name = "UserBuilder", factoryMethod = "aUser")
public class User {

    @BuiltWith(methodName = "named", defaultValue = "\"John Doe\"")
    private String name;

    @BuiltWith(methodName = "withEmail", defaultValue = "\"johnDoe@acme.com\"")
    private String email;

    @BuiltWith(methodName = "withSsn")
    private String ssn;

    @BuiltWith(methodName = "livingInZip", defaultValue = "94114")
    private Integer zipCode;

    @BuiltWith(methodName = "havingAccount", defaultValue = "new Account(\"account_id\")")
    private Account account;

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

    public Account getAccount() {
        return account;
    }
}
