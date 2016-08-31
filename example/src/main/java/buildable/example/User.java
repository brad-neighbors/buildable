package buildable.example;

import buildable.annotation.Buildable;
import buildable.annotation.BuiltWith;

/**
 * An example POJO that demonstrates generating a UserBuilder.
 */
@Buildable(name = "UserBuilder", factoryMethod = "aUser", cloneMethod = "copiedFrom")
public class User {

    @SuppressWarnings("UnusedDeclaration")
    @BuiltWith(methodName = "named", defaultValue = "\"John Doe\"")
    private String name;

    @SuppressWarnings("UnusedDeclaration")
    @BuiltWith(methodName = "withEmail", defaultValue = "\"johnDoe@acme.com\"")
    private String email;

    @SuppressWarnings("UnusedDeclaration")
    @BuiltWith(methodName = "withSsn")
    private String ssn;

    @SuppressWarnings("UnusedDeclaration")
    @BuiltWith(methodName = "livingInZip", defaultValue = "94114")
    private Integer zipCode;

    @SuppressWarnings("UnusedDeclaration")
    @BuiltWith(methodName = "havingAccount", defaultValue = "new Account(\"account_id\")")
    private Account account;

    protected User(){}

    public String getName() {
        return name;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getEmail() {
        return email;
    }

    @SuppressWarnings("UnusedDeclaration")
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
