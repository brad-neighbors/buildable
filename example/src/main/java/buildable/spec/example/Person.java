package buildable.spec.example;


import buildable.example.Account;

import java.util.UUID;

public class Person {

    private String id;
    private String username;
    private Account account;

    public Person(String username) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
