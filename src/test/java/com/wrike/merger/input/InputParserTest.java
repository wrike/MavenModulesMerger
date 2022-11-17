package com.wrike.merger.input;

import com.wrike.merger.MavenModulesMergerException;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Set;

import static com.wrike.merger.input.MergeMode.SOURCES;
import static com.wrike.merger.utils.Constants.MODULE_3_CHILD_1;
import static com.wrike.merger.utils.Constants.SOURCE_MODE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author daniil.shylko on 30.08.2022
 */
public class InputParserTest {

    private static final String NON_EXISTENT_PATH_STRING = "non_existent_path";
    private static final String CORRECT_PATH_STRING = ".";
    private static final Path CORRECT_PATH = Path.of(CORRECT_PATH_STRING);
    private static final String EMPTY_STRING = "";

    @Test
    void checkValidSimpleArguments() {
        InputParser inputParser = new InputParser(MODULE_3_CHILD_1, CORRECT_PATH_STRING, CORRECT_PATH_STRING, SOURCE_MODE);
        assertThat(inputParser)
                .as("Check fields of input parser")
                .extracting(
                        InputParser::getModulesNames,
                        InputParser::getPathToProjectRoot,
                        InputParser::getPathToOutputFile,
                        InputParser::getMergeMode
                ).containsExactly(
                        Set.of(MODULE_3_CHILD_1),
                        CORRECT_PATH,
                        CORRECT_PATH,
                        SOURCES
                );
    }

    @Test
    void checkExceptionForIncorrectNumberOfArguments() {
        assertThatThrownBy(
                InputParser::new
        ).isInstanceOf(MavenModulesMergerException.class)
                .hasMessage("You should pass only four arguments" +
                        " - modulesList, pathToProjectRoot, pathToOutputFile and mergeMode");
    }

    @Test
    void checkExceptionForNonExistentPathToRoot() {
        assertThatThrownBy(
                () -> new InputParser(MODULE_3_CHILD_1, NON_EXISTENT_PATH_STRING, CORRECT_PATH_STRING, SOURCE_MODE)
        ).isInstanceOf(MavenModulesMergerException.class)
                .hasMessage("pathToProjectRoot directory doesn't exist");
    }

    @Test
    void checkExceptionForNullModulesList() {
        assertThatThrownBy(
                () -> new InputParser(null, CORRECT_PATH_STRING, CORRECT_PATH_STRING, SOURCE_MODE)
        ).isInstanceOf(NullPointerException.class)
                .hasMessage("modulesList is marked non-null but is null");
    }

    @Test
    void checkExceptionForNullPathToRoot() {
        assertThatThrownBy(
                () -> new InputParser(MODULE_3_CHILD_1, null, CORRECT_PATH_STRING, SOURCE_MODE)
        ).isInstanceOf(NullPointerException.class)
                .hasMessage("pathToProjectRoot is marked non-null but is null");
    }

    @Test
    void checkExceptionForNullPathToOutputFile() {
        assertThatThrownBy(
                () -> new InputParser(MODULE_3_CHILD_1, CORRECT_PATH_STRING, null, SOURCE_MODE)
        ).isInstanceOf(NullPointerException.class)
                .hasMessage("pathToOutputFile is marked non-null but is null");
    }

    @Test
    void checkExceptionForNullMergeMode() {
        assertThatThrownBy(
                () -> new InputParser(MODULE_3_CHILD_1, CORRECT_PATH_STRING, CORRECT_PATH_STRING, null)
        ).isInstanceOf(NullPointerException.class)
                .hasMessage("mergeMode is marked non-null but is null");
    }

    @Test
    void checkExceptionForEmptyModulesList() {
        assertThatThrownBy(
                () -> new InputParser(EMPTY_STRING, CORRECT_PATH_STRING, CORRECT_PATH_STRING, SOURCE_MODE)
        ).isInstanceOf(MavenModulesMergerException.class)
                .hasMessage("modulesList can't be empty");
    }

    @Test
    void checkExceptionForEmptyPathToRoot() {
        assertThatThrownBy(
                () -> new InputParser(MODULE_3_CHILD_1, EMPTY_STRING, CORRECT_PATH_STRING, SOURCE_MODE)
        ).isInstanceOf(MavenModulesMergerException.class)
                .hasMessage("pathToProjectRoot can't be empty");
    }

    @Test
    void checkExceptionForEmptyPathToOutputFile() {
        assertThatThrownBy(
                () -> new InputParser(MODULE_3_CHILD_1, CORRECT_PATH_STRING, EMPTY_STRING, SOURCE_MODE)
        ).isInstanceOf(MavenModulesMergerException.class)
                .hasMessage("pathToOutputFile can't be empty");
    }

    @Test
    void checkExceptionForEmptyMergeMode() {
        assertThatThrownBy(
                () -> new InputParser(MODULE_3_CHILD_1, CORRECT_PATH_STRING, CORRECT_PATH_STRING, EMPTY_STRING)
        ).isInstanceOf(MavenModulesMergerException.class)
                .hasMessageMatching("Only \\[(sources, target|target, sources)] merge modes are supported, but you provided ``");
    }

    @Test
    void checkExceptionForNonExistentMergeMode() {
        assertThatThrownBy(
                () -> new InputParser(MODULE_3_CHILD_1, CORRECT_PATH_STRING, CORRECT_PATH_STRING, "non_existent_merge_mode")
        ).isInstanceOf(MavenModulesMergerException.class)
                .hasMessageMatching("Only \\[(sources, target|target, sources)] merge modes are supported, but you provided `non_existent_merge_mode`");
    }

}
