package com.wrike.maven_modules_merger.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Author: Daniil Shylko
 * Date: 30.08.2022
 */
@Getter
@AllArgsConstructor
public enum TestProjectDirectory {

    TEST_PROJECT("test_project"),
    TEST_PROJECT_WITH_CONFLICT("test_project_with_conflict"),
    TEST_PROJECT_WITH_BROKEN_TARGET("test_project_with_broken_target");

    private final String directoryName;

}
