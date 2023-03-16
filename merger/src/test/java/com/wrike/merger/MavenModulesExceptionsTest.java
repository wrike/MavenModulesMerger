package com.wrike.merger;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Path;

import static com.wrike.merger.ExitCode.MERGING_FAILED;
import static com.wrike.merger.MockUtils.getMockedExceptionHandler;
import static com.wrike.merger.filter.ExistingFileModulesFilter.filterByAllureProperties;
import static com.wrike.merger.utils.Constants.*;
import static com.wrike.merger.utils.TestFileUtils.createTempTestDirectory;
import static com.wrike.merger.utils.TestProjectDirectory.TEST_PROJECT_WITH_BROKEN_TARGET;
import static com.wrike.merger.utils.TestProjectDirectory.TEST_PROJECT_WITH_CONFLICT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author daniil.shylko on 29.08.2022
 */
public class MavenModulesExceptionsTest {

    @Test
    void checkInputParserExceptionIsCaught() {
        ExceptionHandler mockedExceptionHandler = getMockedExceptionHandler();
        MavenModulesMerger mavenModulesMerger = new MavenModulesMerger(mockedExceptionHandler, filterByAllureProperties());

        mavenModulesMerger.merge(MODULE_3_CHILD_1);

        Mockito.verify(mockedExceptionHandler, times(1)).onException(any(), any());
        Mockito.verify(mockedExceptionHandler).onException(argThat(e ->
                ("You should pass only four arguments" +
                        " - modulesList, pathToProjectRoot, pathToOutputFile and mergeMode").equals(e.getMessage())), any());
    }

    @Test
    void checkExceptionIsLoggedOnce() {
        ExceptionHandler mockedExceptionHandler = getMockedExceptionHandler();
        MavenModulesMerger mavenModulesMerger = new MavenModulesMerger(mockedExceptionHandler, filterByAllureProperties());

        mavenModulesMerger.merge(MODULE_3_CHILD_1);

        Mockito.verify(mockedExceptionHandler, times(1))
                .onException(any(), eq(MERGING_FAILED));
    }

    @Test
    void checkExceptionForFileConflictsWithDifferentContent() {
        Path pathToProjectRoot = createTempTestDirectory(TEST_PROJECT_WITH_CONFLICT);
        Path pathToOutputFile = pathToProjectRoot.resolve(OUTPUT_FILENAME);
        ExceptionHandler mockedExceptionHandler = getMockedExceptionHandler();
        MavenModulesMerger mavenModulesMerger = new MavenModulesMerger(mockedExceptionHandler, filterByAllureProperties());

        mavenModulesMerger.merge("module1/module1_child1,module1/module1_child2", pathToProjectRoot.toString(), pathToOutputFile.toString(), SOURCE_MODE);

        Mockito.verify(mockedExceptionHandler).onException(argThat(e ->
                "Unable to merge files due to conflict in files content.".equals(e.getMessage())), any());
    }

    @Test
    void checkExceptionWhenTargetIsAbsentForTargetMergeMode() {
        Path pathToProjectRoot = createTempTestDirectory(TEST_PROJECT_WITH_BROKEN_TARGET);
        Path pathToOutputFile = pathToProjectRoot.resolve(OUTPUT_FILENAME);
        ExceptionHandler mockedExceptionHandler = getMockedExceptionHandler();
        MavenModulesMerger mavenModulesMerger = new MavenModulesMerger(mockedExceptionHandler, filterByAllureProperties());

        mavenModulesMerger.merge("module1/module1_child1,module2/module2_child1", pathToProjectRoot.toString(), pathToOutputFile.toString(), TARGET_MODE);

        Mockito.verify(mockedExceptionHandler).onException(argThat(e ->
                e.getMessage().matches("No directories found from set \\[(target/classes, target/test-classes|target/test-classes, target/classes)] to merge in `module2/module2_child1` module")), any());
    }

}
