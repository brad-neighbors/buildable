package config;

import buildable.annotation.BuiltWith;
import buildable.config.InjectBuildable;
import buildable.config.BuildableSpec;
import buildable.config.BuildField;


@BuildableSpec
public class BuilderConfig {

    @InjectBuildable
    private ThirdPartyTestObject testObject;

    @InjectBuildable(excludedFields = "ignored")
    private ExcludedFieldTestObject excludedFieldTestObject;

    @InjectBuildable({
            @BuildField(name = "name", value = @BuiltWith(defaultValue = "John")),
            @BuildField(name = "age", value = @BuiltWith(defaultValue = "25")),
            @BuildField(name = "account", value = @BuiltWith(defaultValue = "new Account(\"account_id\")"))
    })
    private DefaultTestObject defaultTestObject;
}
