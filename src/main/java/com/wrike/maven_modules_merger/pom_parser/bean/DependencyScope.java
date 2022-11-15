package com.wrike.maven_modules_merger.pom_parser.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Author: Daniil Shylko
 * Date: 29.08.2022
 */
@Getter
@AllArgsConstructor
public enum DependencyScope {

    SYSTEM("system"),
    COMPILE("compile"),
    RUNTIME("runtime"),
    PROVIDED("provided"),
    TEST("test");

    private final String scopeName;

    private static final Map<String, DependencyScope> dependencyScopesNamesToEnumValues = Arrays.stream(values())
            .collect(Collectors.toMap(DependencyScope::getScopeName, Function.identity()));

    public static Optional<DependencyScope> getScopeByName(String scopeName) {
        return Optional.ofNullable(dependencyScopesNamesToEnumValues.get(scopeName));
    }

}
