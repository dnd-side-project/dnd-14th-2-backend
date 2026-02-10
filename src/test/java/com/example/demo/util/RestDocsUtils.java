package com.example.demo.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RestDocsUtils {

    public static String enumNames(Class<? extends Enum<?>> enumType) {
        return Arrays.stream(enumType.getEnumConstants())
            .map(Enum::name)
            .collect(Collectors.joining(", "));
    }

    public static String allowedValues(Class<? extends Enum<?>> enumType) {
        return "허용 값: [" + enumNames(enumType) + "]";
    }

    public static List<String> enumList(Class<? extends Enum<?>> enumType) {
        return Arrays.stream(enumType.getEnumConstants())
            .map(Enum::name)
            .collect(Collectors.toList());
    }
}
