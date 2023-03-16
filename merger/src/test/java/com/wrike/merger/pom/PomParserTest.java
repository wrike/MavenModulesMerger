package com.wrike.merger.pom;

import com.wrike.merger.pom.bean.Parent;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static com.wrike.merger.pom.utils.XmlUtils.*;
import static com.wrike.merger.utils.Constants.TEST_DIR;
import static com.wrike.merger.utils.Constants.TEST_POM;
import static com.wrike.merger.utils.TestFileUtils.*;
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
                        "groupId",
                        "version"
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
    void checkVersionCanBeGot() {
        PomParser pomParser = new FilePomParser(getTempTestPomPath());
        assertThat(pomParser.getVersion())
                .isEqualTo("1.0-SNAPSHOT");
    }

    @Test
    void checkParentGroupIdCanBeGot() {
        PomParser pomParser = new FilePomParser(getTempTestPomPath());
        assertThat(pomParser.getParentGroupId())
                .isEqualTo("org.company");
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
                .isEqualTo("org.company");
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

    @Test
    void checkParentCanBeSet() {
        FilePomParser pomParser = new FilePomParser(getTempTestChildrenModulesPomPath());

        pomParser.setParent(Parent.builder()
                        .artifactId("test_artifact_id")
                        .groupId("test_group_id")
                        .version("test_version")
                .build());
        pomParser.writeToOriginFile();

        Document document = readXml(pomParser.getOriginPath());
        assertThat(getNodeByXPathTextContent(document, "/project/parent/groupId"))
                .isEqualTo("test_group_id");
        assertThat(getNodeByXPathTextContent(document, "/project/parent/artifactId"))
                .isEqualTo("test_artifact_id");
        assertThat(getNodeByXPathTextContent(document, "/project/parent/version"))
                .isEqualTo("test_version");
    }

}
