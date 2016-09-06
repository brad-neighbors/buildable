package buildable.spec.example;


import java.util.List;

public class Sender extends Person {

    private List<Message> sentMessages;

    private String privateInfo;

    protected Sender() {
        super("User");
    }

    public Sender(String username) {
        super(username);
    }

    public List<Message> getSentMessages() {
        return sentMessages;
    }

    public void setSentMessages(List<Message> sentMessages) {
        this.sentMessages = sentMessages;
    }

    public String getPrivateInfo() {
        return privateInfo;
    }

    public void setPrivateInfo(String privateInfo) {
        this.privateInfo = privateInfo;
    }
}
