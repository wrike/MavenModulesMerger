package com.wrike.pom_parser.utils;

import java.nio.file.Path;
import java.util.List;

/**
 * Author: Daniil Shylko
 * Date: 17.08.2022
 */
public class Constants {

    public static final String TEST_POM = "test_pom.xml";
    public static final String BROKEN_TEST_POM = "broken_test_pom.xml";
    public static final String TEST_CHILDREN_MODULES_POM = "test_children_modules_pom.xml";
    public static final String TEST_DIR = "/test_dir";
    public static final Path INVALID_PATH = Path.of("/non-existent-path-in-root");
    public static final String GROUP_ID = "com.wrike.webtests";
    public static final List<String> TEST_DEPENDENCIES_NAMES = List.of(
            "artifact1",
            "artifact2",
            "artifact3"
    );

    private Constants() {
    }
}
