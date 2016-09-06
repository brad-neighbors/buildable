package buildable.spec.example;

import buildable.annotation.Buildable;
import buildable.annotation.BuiltWith;
import buildable.example.Account;
import buildable.spec.InjectBuildable;
import buildable.spec.BuildableSpec;
import buildable.spec.BuildField;


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

    private Recipient recipient;
}
