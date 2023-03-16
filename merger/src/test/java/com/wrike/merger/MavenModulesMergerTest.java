package com.wrike.merger;

import com.wrike.merger.pom.FilePomParser;
import com.wrike.merger.pom.bean.Dependency;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static com.wrike.merger.MockUtils.getMockedExceptionHandler;
import static com.wrike.merger.filter.ExistingFileModulesFilter.filterByAllureProperties;
import static com.wrike.merger.utils.Constants.*;
import static com.wrike.merger.utils.TestFileUtils.checkMergedDirectoryContainsOnlyContentOfOtherDirectories;
import static com.wrike.merger.utils.TestFileUtils.createTempTestDirectory;
import static com.wrike.merger.utils.TestProjectDirectory.TEST_PROJECT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

/**
 * @author daniil.shylko on 29.08.2022
 */
public class MavenModulesMergerTest {

    private static final Set<Dependency> ALL_DEPENDENCIES = getAllDependencies();

    private Path pathToProjectRoot;
    private Path pathToOutputFile;
    private ExceptionHandler mockedExceptionHandler;
    private MavenModulesMerger mavenModulesMerger;

    @BeforeEach
    void prepare() {
        pathToProjectRoot = createTempTestDirectory(TEST_PROJECT);
        pathToOutputFile = pathToProjectRoot.resolve(OUTPUT_FILENAME);
        mockedExceptionHandler = getMockedExceptionHandler();
        mavenModulesMerger = new MavenModulesMerger(mockedExceptionHandler, filterByAllureProperties());
    }

    @AfterEach
    void checkThatMergerDidNotThrowException() {
        Mockito.verify(mockedExceptionHandler, times(0)).onException(any(), any());
    }

    @Test
    void checkMergerReturnsTheInputForModulesListWithoutCandidatesForMerging() {
        String modulesList = "moduleA,moduleB,moduleC";

        mavenModulesMerger.merge(modulesList, pathToProjectRoot.toString(), pathToOutputFile.toString(), SOURCE_MODE);

        checkMergingWasNotPerformed();
        checkOutputFileHasModulesList(Set.of(modulesList.split(",")));
    }

    @Test
    void checkMergerReturnsTheInputForModulesListWithOnlyOneCandidateForMerging() {
        String modulesList = "moduleA,moduleB,moduleC,module1/module1_child1";

        mavenModulesMerger.merge(modulesList, pathToProjectRoot.toString(), pathToOutputFile.toString(), SOURCE_MODE);

        checkMergingWasNotPerformed();
        checkOutputFileHasModulesList(Set.of(modulesList.split(",")));
    }

    @Test
    void checkMergingDuplicateModules() {
        mavenModulesMerger.merge(MODULE_3_CHILD_1 + "," + MODULE_3_CHILDREN, pathToProjectRoot.toString(), pathToOutputFile.toString(), SOURCE_MODE);

        checkModule3WasMerged();
    }

    @Test
    void checkMergingTwoModules() {
        mavenModulesMerger.merge(MODULE_3_CHILDREN, pathToProjectRoot.toString(), pathToOutputFile.toString(), SOURCE_MODE);

        checkModule3WasMerged();
    }

    @Test
    void checkMergingAllModulesTogetherForSourceMergeMode() {
        mavenModulesMerger.merge(ALL_MODULES, pathToProjectRoot.toString(), pathToOutputFile.toString(), SOURCE_MODE);

        checkMergedDirectoryContainsOnlyContentOfOtherDirectories(
                getModuleSrcDirectory(MERGED_MODULES),
                List.of(
                        getModuleSrcDirectory("module1/module1_child1"),
                        getModuleSrcDirectory("module1/module1_child2"),
                        getModuleSrcDirectory("module2"),
                        getModuleSrcDirectory(MODULE_3_CHILD_1),
                        getModuleSrcDirectory(MODULE_3_CHILD_2)
                )
        );
        checkMergedModulesHasDependencies(ALL_DEPENDENCIES);
        checkMergedModulesIsChildOfParentPom();
        checkOutputFileHasModulesList(Set.of(MERGED_MODULES, MODULE_WITHOUT_ALLURE_PROPERTIES));
    }

    @Test
    void checkMergingAllRootModulesTogetherForTargetMergeMode() {
        mavenModulesMerger.merge(ALL_MODULES, pathToProjectRoot.toString(), pathToOutputFile.toString(), TARGET_MODE);

        checkMergedDirectoryContainsOnlyContentOfOtherDirectories(
                getModuleTargetDirectory(MERGED_MODULES).resolve(CLASSES),
                List.of(
                        getModuleTargetDirectory(MODULE_3_CHILD_2).resolve(CLASSES),
                        getModuleTargetDirectory("module1/module1_child1").resolve(CLASSES),
                        getModuleTargetDirectory("module1/module1_child2").resolve(CLASSES),
                        getModuleTargetDirectory("module2").resolve(CLASSES)
                )
        );
        checkMergedDirectoryContainsOnlyContentOfOtherDirectories(
                getModuleTargetDirectory(MERGED_MODULES).resolve(TEST_CLASSES),
                List.of(
                        getModuleTargetDirectory(MODULE_3_CHILD_1).resolve(TEST_CLASSES),
                        getModuleTargetDirectory("module1/module1_child1").resolve(TEST_CLASSES),
                        getModuleTargetDirectory("module1/module1_child2").resolve(TEST_CLASSES),
                        getModuleTargetDirectory("module2").resolve(TEST_CLASSES)
                )
        );
        checkMergedModulesHasDependencies(ALL_DEPENDENCIES);
        checkMergedModulesIsChildOfParentPom();
        checkOutputFileHasModulesList(Set.of(MERGED_MODULES, MODULE_WITHOUT_ALLURE_PROPERTIES));
    }

    @Test
    void checkOnlySrcDirectoryIsMergedForSourceMergeMode() throws IOException {
        mavenModulesMerger.merge(MODULE_3_CHILDREN, pathToProjectRoot.toString(), pathToOutputFile.toString(), SOURCE_MODE);

        assertThat(Files.list(pathToProjectRoot.resolve(MERGED_MODULES)))
                .as("Check merged_modules does not contain files except of src folder and pom file")
                .containsExactlyInAnyOrder(getModuleSrcDirectory(MERGED_MODULES), getModulePomFile(MERGED_MODULES));
    }

    @Test
    void checkOnlyTargetClassesAreMergedForTargetMergeMode() throws IOException {
        mavenModulesMerger.merge(MODULE_3_CHILDREN, pathToProjectRoot.toString(), pathToOutputFile.toString(), TARGET_MODE);

        Path targetDirectory = getModuleTargetDirectory(MERGED_MODULES);
        assertThat(Files.list(pathToProjectRoot.resolve(MERGED_MODULES)))
                .as("Check merged_modules does not contain files except of target folder and pom file")
                .containsExactlyInAnyOrder(targetDirectory, getModulePomFile(MERGED_MODULES));
        assertThat(Files.list(targetDirectory))
                .as("Check merged_modules target directory contains only classes and test-classes")
                .containsExactlyInAnyOrder(targetDirectory.resolve("classes"), targetDirectory.resolve("test-classes"));
    }

    @Test
    void checkMergedModulesAreWrittenIfParentDirectoryDoesNotExist() {
        pathToOutputFile = pathToOutputFile.getParent().resolve("non_existent_parent_directory").resolve(OUTPUT_FILENAME);
        mavenModulesMerger.merge(MODULE_3_CHILDREN, pathToProjectRoot.toString(), pathToOutputFile.toString(), SOURCE_MODE);

        checkModule3WasMerged();
    }

    private void checkModule3WasMerged() {
        checkMergedDirectoryContainsOnlyContentOfOtherDirectories(
                getModuleSrcDirectory(MERGED_MODULES),
                List.of(
                        getModuleSrcDirectory(MODULE_3_CHILD_1),
                        getModuleSrcDirectory(MODULE_3_CHILD_2)
                )
        );
        checkMergedModulesHasDependencies(Set.of(
                testDependency("dependency1", false),
                testDependency("dependency10", true),
                testDependency("dependency15", true),
                testDependency("dependency16", true),
                testDependency("dependency17", true),
                testDependency("dependency18", true)
        ));
        checkMergedModulesIsChildOfParentPom();
        checkOutputFileHasModulesList(Set.of(MERGED_MODULES));
    }

    private Path getModuleSrcDirectory(String moduleName) {
        return pathToProjectRoot.resolve(moduleName).resolve(SRC);
    }

    private Path getModuleTargetDirectory(String moduleName) {
        return pathToProjectRoot.resolve(moduleName).resolve("target");
    }

    private Path getModulePomFile(String moduleName) {
        return pathToProjectRoot.resolve(moduleName).resolve(POM_FILENAME);
    }

    private void checkMergedModulesHasDependencies(Set<Dependency> dependencies) {
        assertThat(new FilePomParser(getModulePomFile(MERGED_MODULES)).getAllDependencies())
                .as("Check merged_modules pom file dependencies")
                .isEqualTo(dependencies);
    }

    private void checkMergedModulesIsChildOfParentPom() {
        assertThat(new FilePomParser(pathToProjectRoot.resolve(POM_FILENAME)).getChildrenModules())
                .as("Check merged_modules is a child of the parent module")
                .contains(MERGED_MODULES);
    }

    private void checkMergingWasNotPerformed() {
        checkMergedModulesModuleIsAbsent();
        checkMergedModulesIsNotChildOfParentPom();
    }

    private void checkMergedModulesModuleIsAbsent() {
        assertThat(pathToProjectRoot.resolve(MERGED_MODULES))
                .as("Check merged_modules module is absent")
                .doesNotExist();
    }

    private void checkMergedModulesIsNotChildOfParentPom() {
        assertThat(new FilePomParser(pathToProjectRoot.resolve(POM_FILENAME)).getChildrenModules())
                .as("Check merged_modules is not a child of the parent module")
                .doesNotContain(MERGED_MODULES);
    }

    private void checkOutputFileHasModulesList(Set<String> modulesList) {
        try {
            assertThat(Files.readString(pathToOutputFile).split(","))
                    .as("Check output file modules list")
                    .containsExactlyInAnyOrderElementsOf(modulesList);
        } catch (IOException e) {
            throw new IllegalStateException("Can't open output file", e);
        }
    }

    private static Set<Dependency> getAllDependencies() {
        return Set.of(
                testDependency("dependency1", false),
                testDependency("dependency2", false),
                testDependency("dependency3", true),
                testDependency("dependency4", true),
                testDependency("dependency5", true),
                testDependency("dependency6", true),
                testDependency("dependency7", true),
                testDependency("dependency8", true),
                testDependency("dependency9", true),
                testDependency("dependency10", true),
                testDependency("dependency11", true),
                testDependency("dependency12", true),
                testDependency("dependency13", true),
                testDependency("dependency14", true),
                testDependency("dependency15", true),
                testDependency("dependency16", true),
                testDependency("dependency17", true),
                testDependency("dependency18", true)
        );
    }

    private static Dependency testDependency(String artifactId, boolean hasVersion) {
        return Dependency.builder()
                .groupId("org.company")
                .artifactId(artifactId)
                .version(hasVersion ? "1.0-SNAPSHOT" : null)
                .build();
    }

}
