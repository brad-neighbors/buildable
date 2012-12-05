package com.incandescent.buildable.example;

import org.junit.Test;

import static com.incandescent.buildable.example.AccountBuilder.anAccount;
import static junit.framework.Assert.assertEquals;

public class BuildableWithSubclassTest {

    @Test public void canBuildAccountWithIdentifier() {
        Account identifiedAccount = anAccount().identifiedBy("123").build();
        assertEquals("123", identifiedAccount.getId());
    }
}
