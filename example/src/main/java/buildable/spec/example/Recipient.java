package buildable.spec.example;


import java.util.Set;

public class Recipient extends Person {

    private Set<Message> inbox;

    public Recipient(String username) {
        super(username);
    }

    public Set<Message> getInbox() {
        return inbox;
    }

    public void setInbox(Set<Message> inbox) {
        this.inbox = inbox;
    }
}
