package buildable.example;

import buildable.annotation.Buildable;
import buildable.annotation.BuiltWith;

@Buildable(name = "AdminBuilder", makeAbstract = true)
public class Admin {

    @SuppressWarnings("UnusedDeclaration")
    @BuiltWith(methodName = "named", defaultValue = "\"suzieAdmin\"")
    private String name;

    protected Admin(){}

    public String getName() {
        return name;
    }
}
