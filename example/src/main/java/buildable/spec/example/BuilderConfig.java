package buildable.spec.example;

import buildable.annotation.Buildable;
import buildable.annotation.BuiltWith;
import buildable.spec.BuildConstructor;
import buildable.spec.BuildField;
import buildable.spec.BuildableSpec;
import buildable.spec.ConstructorArg;
import buildable.spec.InjectBuildable;


@BuildableSpec
public class BuilderConfig {

    @InjectBuildable(value = @Buildable(factoryMethod = "aNewMessage", cloneMethod = "copyOf"),
            excludedFields = "timestamp",
            fields = {
                    @BuildField(name = "text", value = @BuiltWith(defaultValue = "hello")),
                    @BuildField(name = "id", value = @BuiltWith(defaultValue = "java.util.UUID.randomUUID()")),
            }
    )
    private Message message;

    @InjectBuildable(value = @Buildable(makeAbstract = true))
    private Person person;

    @InjectBuildable(excludedFields = "privateInfo")
    private Sender sender;

    @InjectBuildable(excludedFields = "username")
    @BuildConstructor({
        @ConstructorArg(name = "firstName", type = String.class, value = @BuiltWith(methodName = "named")),
        @ConstructorArg(name = "lastName", type = String.class),
    })
    private Recipient recipient;

    @InjectBuildable(fields = @BuildField(name = "name", value = @BuiltWith(methodName = "named")))
    private Broker broker;
}
