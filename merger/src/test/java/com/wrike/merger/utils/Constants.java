package com.wrike.merger.utils;

import java.nio.file.Path;
import java.util.List;

/**
 * @author daniil.shylko on 30.08.2022
 */
public class Constants {

    public static final String TEST_POM = "test_pom.xml";
    public static final String BROKEN_TEST_POM = "broken_test_pom.xml";
    public static final String TEST_CHILDREN_MODULES_POM = "test_children_modules_pom.xml";
    public static final String TEST_DIR = "/test_dir";
    public static final Path INVALID_PATH = Path.of("/non-existent-path-in-root");
    public static final String GROUP_ID = "org.company";
    public static final List<String> TEST_DEPENDENCIES_NAMES = List.of(
            "artifact1",
            "artifact2",
            "artifact3"
    );

    public static final String SRC = "src";
    public static final String POM_FILENAME = "pom.xml";
    public static final String MERGED_MODULES = "merged_modules";
    public static final String MODULE_3_CHILDREN = "module3/module3_child1,module3/module3_child2";
    public static final String MODULE_3_CHILD_1 = "module3/module3_child1";
    public static final String MODULE_3_CHILD_2 = "module3/module3_child2";
    public static final String MODULE_WITHOUT_ALLURE_PROPERTIES = "module_without_allure_properties";
    public static final String ALL_MODULES = "module1/module1_child1,module1/module1_child2,module2,module3/module3_child1,module3/module3_child2,module_without_allure_properties";
    public static final String OUTPUT_FILENAME = "modulesList.txt";
    public static final String SOURCE_MODE = "sources";
    public static final String TARGET_MODE = "target";
    public static final String CLASSES = "classes";
    public static final String TEST_CLASSES = "test-classes";

    private Constants(){
    }

}
