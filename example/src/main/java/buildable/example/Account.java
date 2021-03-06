package buildable.example;

import buildable.annotation.Buildable;
import buildable.annotation.BuiltWith;
import buildable.spec.example.Broker;

/**
 * An example POJO that will help demonstrate the @BuiltWith annotations work when included in superclasses.
 */
@Buildable
public class Account extends Identified {

    private static final String EMPTY_ACCOUNT_ID = "unassigned";

    @SuppressWarnings("UnusedDeclaration")
    @BuiltWith(methodName = "named")
    private String name;

    private Broker broker;

    public Account(String id){
        super(id);
    }

    @SuppressWarnings("UnusedDeclaration")
    protected Account(){
        super(EMPTY_ACCOUNT_ID);
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getName() {
        return name;
    }

    public Broker getBroker() {
        return broker;
    }
}
