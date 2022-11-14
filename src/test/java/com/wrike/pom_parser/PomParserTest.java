package com.wrike.pom_parser;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static com.wrike.pom_parser.utils.Constants.TEST_DIR;
import static com.wrike.pom_parser.utils.Constants.TEST_POM;
import static com.wrike.pom_parser.utils.FileUtils.checkFilesContentIsEqualIgnoringWhitespace;
import static com.wrike.pom_parser.utils.FileUtils.getTempTestPomPath;
import static com.wrike.pom_parser.utils.XmlUtils.getNodesNames;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Author: Daniil Shylko
 * Date: 04.08.2022
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
                        "dependencies"
                );
    }

    @Test
    void checkArtifactIdCanBeGot() {
        PomParser pomParser = new FilePomParser(getTempTestPomPath());
        assertThat(pomParser.getArtifactId())
                .isEqualTo("test_pom");
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
