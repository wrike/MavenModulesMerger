package com.wrike.maven_modules_merger.pom_parser;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static com.wrike.maven_modules_merger.pom_parser.utils.XmlUtils.getNodesNames;
import static com.wrike.maven_modules_merger.utils.Constants.TEST_DIR;
import static com.wrike.maven_modules_merger.utils.Constants.TEST_POM;
import static com.wrike.maven_modules_merger.utils.TestFileUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author daniil.shylko on 04.08.2022
 */
public class PomParserTest {

    @Test
    void checkAllPomsCanBeCollected() throws URISyntaxException {
        Path testDirectory = Path.of(Objects.requireNonNull(getClass().getResource(TEST_DIR)).toURI());
        List<FilePomParser> pomParsers = FilePomParser.collectAllPomFiles(testDirectory);
        assertThat(pomParsers)
                .extracting(FilePomParser::getOriginPath)
                .containsExactlyInAnyOrder(
                        testDirectory.resolve("subdir1/pom.xml"),
                        testDirectory.resolve("subdir2/pom.xml"),
                        testDirectory.resolve("subdir2/subdir3/pom.xml")
                );
    }

    @Test
    void checkAllProjectChildrenCanBeCollected() {
        PomParser pomParser = new FilePomParser(getTempTestPomPath());
        assertThat(getNodesNames(pomParser.getProjectChildren()))
                .containsExactlyInAnyOrder(
                        "parent",
                        "modelVersion",
                        "artifactId",
                        "dependencies",
                        "groupId"
                );
    }

    @Test
    void checkArtifactIdCanBeGot() {
        PomParser pomParser = new FilePomParser(getTempTestPomPath());
        assertThat(pomParser.getArtifactId())
                .isEqualTo("test_pom");
    }

    @Test
    void checkGroupIdCanBeGot() {
        PomParser pomParser = new FilePomParser(getTempTestPomPath());
        assertThat(pomParser.getGroupId())
                .isEqualTo("groupId");
    }

    @Test
    void checkParentGroupIdCanBeGot() {
        PomParser pomParser = new FilePomParser(getTempTestPomPath());
        assertThat(pomParser.getParentGroupId())
                .isEqualTo("com.wrike.webtests");
    }

    @Test
    void checkEffectiveGroupIdIfGroupIdExist() {
        PomParser pomParser = new FilePomParser(getTempTestPomPath());
        assertThat(pomParser.getEffectiveGroupId())
                .isEqualTo("groupId");
    }

    @Test
    void checkEffectiveGroupIdIfGroupIdAbsent() {
        PomParser pomParser = new FilePomParser(getTempTestChildrenModulesPomPath());
        assertThat(pomParser.getEffectiveGroupId())
                .isEqualTo("com.wrike.webtests");
    }

    @Test
    void checkPomParserCanBeWrittenToOriginFile() throws IOException {
        Path originTestPomPath = getTempTestPomPath();
        Path actualTempTestPomPath = getTempTestPomPath();
        FilePomParser expectedPomParser = new FilePomParser(originTestPomPath);
        expectedPomParser.writeToOriginFile();
        checkFilesContentIsEqualIgnoringWhitespace(originTestPomPath, actualTempTestPomPath);
    }

    @Test
    void checkPomParserCanBeWrittenToAnotherFile() throws IOException {
        Path originTestPomPath = getTempTestPomPath();
        Path anotherTestPomPath = getTempTestPomPath();
        PomParser expectedPomParser = new FilePomParser(originTestPomPath);
        expectedPomParser.writeToFile(anotherTestPomPath);
        checkFilesContentIsEqualIgnoringWhitespace(originTestPomPath, anotherTestPomPath);
    }

    @Test
    void checkPomParserCanBeWrittenToAnotherFileIfParentDirectoryDoesNotExist() throws IOException {
        Path originTestPomPath = getTempTestPomPath();
        Path testPomPathWithNonExistentDirectory = originTestPomPath.getParent()
                .resolve("non_existent_parent_directory").resolve(TEST_POM);
        PomParser expectedPomParser = new FilePomParser(originTestPomPath);
        expectedPomParser.writeToFile(testPomPathWithNonExistentDirectory);
        checkFilesContentIsEqualIgnoringWhitespace(originTestPomPath, testPomPathWithNonExistentDirectory);
    }

}
