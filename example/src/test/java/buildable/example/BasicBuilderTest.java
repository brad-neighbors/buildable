package buildable.example;

import buildable.spec.example.BrokerBuilder;
import buildable.spec.example.Sender;
import org.junit.Test;

import static buildable.example.AccountBuilder.anAccount;
import static buildable.example.UserBuilder.aUser;
import static buildable.spec.example.MessageBuilder.aNewMessage;
import static buildable.spec.example.SenderBuilder.aSender;
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

    @Test public void canBuildUserWithAccountByPassingInAccountBuilder() {
        assertEquals("Account matches", "foo", aUser().havingAccount(anAccount().identifiedBy("foo")).build()
                .getAccount().getId());
    }

    @Test public void canBuildMessageWithTest() {
        assertEquals("Message Text matches", "Some message", aNewMessage().withText("Some message").build().getText());
    }

    @Test public void canBuildMessageByPassingInSenderInstance() {
        Sender sender = new Sender("jDoe");
        assertEquals("Sender matches", "jDoe", aNewMessage().withSender(sender).build().getSender().getUsername());
    }

    @Test public void canBuildMessageByPassingInSenderBuilder() {
        assertEquals("Sender matches", "jDoe", aNewMessage().withSender(aSender().withUsername("jDoe")).build().getSender().getUsername());
    }

    @Test public void canBuildSenderByPassingInAccountBuilder() {
        assertEquals("Account matches", "foo", aSender().withAccount(anAccount().identifiedBy("foo")).build().getAccount().getId());
    }

    @Test public void canBuildAccountByPassingInBrokerBuilder() {
        assertEquals("Broker matches", "bar", anAccount().withBroker(BrokerBuilder.aBroker().named("bar")).build().getBroker().getName());
    }
}
