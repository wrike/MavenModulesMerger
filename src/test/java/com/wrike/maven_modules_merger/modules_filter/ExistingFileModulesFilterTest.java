package com.wrike.maven_modules_merger.modules_filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static com.wrike.maven_modules_merger.modules_filter.ExistingFileModulesFilter.filterByAllureProperties;
import static com.wrike.maven_modules_merger.utils.Constants.MODULE_WITHOUT_ALLURE_PROPERTIES;
import static com.wrike.maven_modules_merger.utils.TestFileUtils.createTempTestDirectory;
import static com.wrike.maven_modules_merger.utils.TestProjectDirectory.TEST_PROJECT;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Author: Daniil Shylko
 * Date: 15.11.2022
 */
public class ExistingFileModulesFilterTest {

    private Path pathToProjectRoot;

    @BeforeEach
    void prepare() {
        pathToProjectRoot = createTempTestDirectory(TEST_PROJECT);
    }

    @Test
    void checkFilterReturnsTrueForModuleWithFile() {
        assertThat(filterByAllureProperties().moduleMatches(pathToProjectRoot.resolve("module2")))
                .as("Check filter returns true for module with file")
                .isTrue();
    }

    @Test
    void checkFilterReturnsFalseForModuleWithoutFile() {
        assertThat(filterByAllureProperties().moduleMatches(pathToProjectRoot.resolve(MODULE_WITHOUT_ALLURE_PROPERTIES)))
                .as("Check filter returns true for module with file")
                .isFalse();
    }

}
