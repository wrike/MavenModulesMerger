package com.wrike.maven_modules_merger.modules_filter;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Filter modules by existing file
 *
 * @author daniil.shylko on 15.11.2022
 */
public class ExistingFileModulesFilter implements ModulesFilter {

    private final Path relativeFilePath;

    public ExistingFileModulesFilter(Path relativeFilePath) {
        this.relativeFilePath = relativeFilePath;
    }

    @Override
    public boolean moduleMatches(Path modulePath) {
        return Files.exists(modulePath.resolve(relativeFilePath));
    }

    public static ExistingFileModulesFilter filterByAllureProperties() {
        return new ExistingFileModulesFilter(Path.of("src/test/resources/allure.properties"));
    }
}
