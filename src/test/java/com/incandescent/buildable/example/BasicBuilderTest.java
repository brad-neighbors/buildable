package com.incandescent.buildable.example;

import org.junit.Test;

import static com.incandescent.buildable.example.AccountBuilder.anAccount;
import static com.incandescent.buildable.example.UserBuilder.aUser;
import static junit.framework.Assert.assertEquals;

public class BasicBuilderTest {

    @Test public void canBuildUserWithStringName() {
        assertEquals("Name matches", "John Doe", aUser().named("John Doe").build().getName());
    }

    @Test public void canBuildUserWithIntegerZipcode() {
        assertEquals("Zipcode matches", Integer.valueOf(94114), aUser().livingInZip(94114).build().getZipCode());
    }

    @Test public void canBuildUserWithDefaultName() {
        assertEquals("Default name matches what is specified in annotation", "John Doe", aUser().build().getName());
    }

    @Test public void canBuildUserWithAccountByPassingInAccountInstance() {
        Account account = new Account("my account id");
        assertEquals("Account matches", account.getId(), aUser().havingAccount(account).build().getAccount().getId());
    }

    @Test public void canBuilderUserWithAccountByPassingInAccountBuilder() {
        assertEquals("Account matches", "foo", aUser().havingAccount(anAccount().identifiedBy("foo")).build()
                .getAccount().getId());
    }
}
