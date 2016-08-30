package buildable.config.processor;

import buildable.annotation.BuiltWith;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

import static buildable.annotation.processor.Util.capitalize;

/**
 * Created by bduong on 8/30/16.
 */
public class ClassWriterUtil {
    /**
     *
     * @param builder
     * @param buildableField
     * @param fieldName
     * @param builtWith
     * @param fieldClassName
     * @param builderName
     */
    static void buildFieldMethod(TypeSpec.Builder builder, VariableElement buildableField, String fieldName,
                                 BuiltWith builtWith, TypeName fieldClassName, ClassName builderName) {
        FieldSpec.Builder fieldBuilder = FieldSpec.builder(fieldClassName, fieldName, Modifier.PRIVATE);

        if (builtWith != null) {
            if (!builtWith.defaultValue().equals(BuiltWith.USE_SENSIBLE_DEFAULT)) {
                String sub = "java.lang.String".equals(fieldClassName.toString()) ? "$S" : "$L"; //If the Class of the field is String use a string substitution otherwise use a literal.
                fieldBuilder.initializer(sub, builtWith.defaultValue());
            }
        }

        builder.addField(fieldBuilder.build());
        builder.addMethod(MethodSpec
                .methodBuilder(determineFluentMethodName(buildableField))
                .addModifiers(Modifier.PUBLIC)
                .returns(builderName)
                .addParameter(fieldClassName, fieldName)
                .addStatement("this.$L = $L", fieldName, fieldName)
                .addStatement("return this")
                .build()
        );
    }

    private static String determineFluentMethodName(final VariableElement field) {
        return "with" + capitalize(field.getSimpleName());
    }
}
