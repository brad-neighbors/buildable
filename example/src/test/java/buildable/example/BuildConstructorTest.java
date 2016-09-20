package buildable.example;

import buildable.spec.example.RecipientBuilder;
import org.junit.Test;

import static buildable.spec.example.RecipientBuilder.*;
import static org.junit.Assert.assertEquals;

public class BuildConstructorTest {

    @Test
    public void callsConstructorWhenBuilding() {
        assertEquals("Username Matches", "John Doe", aRecipient().named("John").withLastName("Doe").build().getUsername());
    }
}
