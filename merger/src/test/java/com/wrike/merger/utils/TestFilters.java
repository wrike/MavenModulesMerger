package com.wrike.merger.utils;

import com.wrike.merger.filter.ExistingFileModulesFilter;

import java.nio.file.Path;

/**
 * @author daniil.shylko on 16.03.2023
 */
public class TestFilters {

    public static ExistingFileModulesFilter filterByAllureProperties() {
        return new ExistingFileModulesFilter(Path.of("src/test/resources/allure.properties"));
    }
}
