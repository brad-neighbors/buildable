package buildable.example;

import org.junit.Test;

import static buildable.example.AccountBuilder.anAccount;
import static buildable.example.UserBuilder.aUser;
import static junit.framework.Assert.assertNotNull;

public class MultipleBuildablesTest {

    @Test public void multipleClassesAnnotatedEachHaveBuilderGenerated() {
        assertNotNull("UserBuilder generated and builds a user", aUser().build());
        assertNotNull("AccountBuilder generated and builds an account", anAccount().build());
    }
}
