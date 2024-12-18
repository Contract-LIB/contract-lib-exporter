package org.contractlib.verifast;

import org.contractlib.ast.Type;
import org.stringtemplate.v4.AttributeRenderer;

import java.util.Locale;

public class TypeRenderer implements AttributeRenderer<Type> {
    @Override
    public String toString(Type value, String formatString, Locale locale) {
        return switch(value) {
            case Type.Sort sort -> correctKnown(sort.name()) +
                    sort.arguments().stream().map(a -> toString(a, formatString, locale)).reduce((a, b) -> a + ", " + b).map(a -> "<" + a + ">").orElse("");
            case Type.Param param -> correctKnown(param.name());
        };
    }

    public String correctKnown(String name) {
        return switch(name) {
            case "Int" -> "int";
            case "Bool" -> "boolean";
            case "string" -> "string";
            default -> name;
        };
    }
}
