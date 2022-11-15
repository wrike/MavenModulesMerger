package com.wrike.maven_modules_merger.pom_parser;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.wrike.maven_modules_merger.pom_parser.utils.XmlUtils.*;
import static com.wrike.maven_modules_merger.utils.Constants.*;
import static com.wrike.maven_modules_merger.utils.TestFileUtils.checkFilesContentIsEqualIgnoringWhitespace;
import static com.wrike.maven_modules_merger.utils.TestFileUtils.getTempTestPomPath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author daniil.shylko on 18.08.2022
 */
public class XmlUtilTest {

    private static final String NOT_FOUND_XPATH = "/projectaaa";
    private static final String INVALID_XPATH = "(";

    @Test
    void checkXmlCanBeReadAndWritten() throws IOException {
        Path tempTestPomPath = getTempTestPomPath();
        Path actualTempTestPomPath = getTempTestPomPath();
        Document document = readXml(tempTestPomPath);
        Files.delete(tempTestPomPath);
        writeXml(document, tempTestPomPath);
        checkFilesContentIsEqualIgnoringWhitespace(tempTestPomPath, actualTempTestPomPath);
    }

    @Test
    void checkXmlCanBeWrittenToFileIfParentDirectoryDoesNotExist() throws IOException {
        Path tempTestPomPath = getTempTestPomPath();
        Path testPomPathWithNonExistentDirectory = tempTestPomPath.getParent()
                .resolve("non_existent_parent_directory").resolve(TEST_POM);
        Path actualTempTestPomPath = getTempTestPomPath();
        Document document = readXml(tempTestPomPath);
        Files.delete(tempTestPomPath);
        writeXml(document, testPomPathWithNonExistentDirectory);
        checkFilesContentIsEqualIgnoringWhitespace(testPomPathWithNonExistentDirectory, actualTempTestPomPath);
    }

    @Test
    void checkXmlCanBeReadFromInputStream() throws IOException {
        Path tempTestPomPath = getTempTestPomPath();
        Path actualTempTestPomPath = getTempTestPomPath();
        Document document = readXml(new FileInputStream(tempTestPomPath.toFile()));
        Files.delete(tempTestPomPath);
        writeXml(document, tempTestPomPath);
        checkFilesContentIsEqualIgnoringWhitespace(tempTestPomPath, actualTempTestPomPath);
    }

    @Test
    void checkXmlCanBeReadAndWrittenWithPrettify() throws IOException {
        Path tempTestPomPath = getTempTestPomPath();
        Path actualTempTestPomPath = getTempTestPomPath();
        Document document = readXml(tempTestPomPath);
        Files.delete(tempTestPomPath);
        writePrettifiedXml(document, tempTestPomPath);
        checkFilesContentIsEqualIgnoringWhitespace(tempTestPomPath, actualTempTestPomPath);
    }

    @Test
    void checkPomParserReadingFromInvalidFileCauseException() {
        assertThatThrownBy(
                () -> readXml(INVALID_PATH)
        ).isInstanceOf(PomParserException.class)
                .hasMessage(String.format("Unable to read xml from `%s` file", INVALID_PATH));
    }

    @Test
    void checkGetNodeByXPathReturnsSingleNode() {
        Path tempTestPomPath = getTempTestPomPath();
        Document document = readXml(tempTestPomPath);
        assertThat(getNodeByXPath(document, "/project/artifactId").getTextContent())
                .isEqualTo("test_pom");
    }

    @Test
    void checkGetNodeByXPathReturnsFirstNodeForMultipleResults() {
        Path tempTestPomPath = getTempTestPomPath();
        Document document = readXml(tempTestPomPath);
        assertThat(getNodeByXPath(document, "/project/dependencies/dependency/artifactId").getTextContent())
                .isEqualTo(TEST_DEPENDENCIES_NAMES.get(0));
    }

    @Test
    void checkGetNodeByXPathReturnsNullIfNodeNotFound() {
        Path tempTestPomPath = getTempTestPomPath();
        Document document = readXml(tempTestPomPath);
        assertThat(getNodeByXPath(document, NOT_FOUND_XPATH))
                .isNull();
    }

    @Test
    void checkGetNodeByXPathThrowsExceptionForInvalidXPath() {
        Path tempTestPomPath = getTempTestPomPath();
        Document document = readXml(tempTestPomPath);
        assertThatThrownBy(
                () -> getNodeByXPath(document, INVALID_XPATH)
        ).isInstanceOf(PomParserException.class)
                .hasMessage("Unable to execute `(` XPath");
    }

    @Test
    void checkGetNodesByXPathReturnsSingleNode() {
        Path tempTestPomPath = getTempTestPomPath();
        Document document = readXml(tempTestPomPath);
        assertThat(getNodesByXPath(document, "/project/artifactId"))
                .extracting(Node::getTextContent)
                .isEqualTo(List.of("test_pom"));
    }

    @Test
    void checkGetNodeByXPathReturnsMultipleNodes() {
        Path tempTestPomPath = getTempTestPomPath();
        Document document = readXml(tempTestPomPath);
        assertThat(getNodesByXPath(document, "/project/dependencies/dependency/artifactId"))
                .extracting(Node::getTextContent)
                .isEqualTo(TEST_DEPENDENCIES_NAMES);
    }

    @Test
    void checkGetNodeByXPathReturnsEmptyListIfNodeNotFound() {
        Path tempTestPomPath = getTempTestPomPath();
        Document document = readXml(tempTestPomPath);
        assertThat(getNodesByXPath(document, NOT_FOUND_XPATH))
                .isEmpty();
    }

    @Test
    void checkGetNodeByXPathTextContent() {
        Path tempTestPomPath = getTempTestPomPath();
        Document document = readXml(tempTestPomPath);
        assertThat(getNodeByXPathTextContent(document, "/project/artifactId"))
                .isEqualTo("test_pom");
    }

    @Test
    void checkGetNodeByXPathTextContentForNonExistentNode() {
        Path tempTestPomPath = getTempTestPomPath();
        Document document = readXml(tempTestPomPath);
        assertThat(getNodeByXPathTextContent(document, "/project/broken_xpath"))
                .isNull();
    }

    @Test
    void checkGetNodesByXPathThrowsExceptionForInvalidXPath() {
        Path tempTestPomPath = getTempTestPomPath();
        Document document = readXml(tempTestPomPath);
        assertThatThrownBy(
                () -> getNodesByXPath(document, INVALID_XPATH)
        ).isInstanceOf(PomParserException.class)
                .hasMessage("Unable to execute `(` XPath");
    }

    @Test
    void checkRemoveNodesByXPathForSingleNode() {
        Path tempTestPomPath = getTempTestPomPath();
        Document document = readXml(tempTestPomPath);
        assertThat(removeNodesByXPath(document, "/project/artifactId"))
                .isEqualTo(1);
        assertThat(getNodesByXPath(document, "/project/artifactId"))
                .isEmpty();
    }

    @Test
    void checkRemoveNodesByXPathForMultipleNodes() {
        Path tempTestPomPath = getTempTestPomPath();
        Document document = readXml(tempTestPomPath);
        assertThat(removeNodesByXPath(document, "/project/dependencies/dependency/artifactId"))
                .isEqualTo(3);
        assertThat(getNodesByXPath(document, "/project/dependencies/dependency/artifactId"))
                .isEmpty();
    }

    @Test
    void checkRemoveNodesByXPathReturnsZeroIfNodeNotFound() {
        Path tempTestPomPath = getTempTestPomPath();
        Document document = readXml(tempTestPomPath);
        assertThat(removeNodesByXPath(document, NOT_FOUND_XPATH))
                .isEqualTo(0);
    }

    @Test
    void checkRemoveNodesByXPathThrowsExceptionForInvalidXPath() {
        Path tempTestPomPath = getTempTestPomPath();
        Document document = readXml(tempTestPomPath);
        assertThatThrownBy(
                () -> removeNodesByXPath(document, INVALID_XPATH)
        ).isInstanceOf(PomParserException.class)
                .hasMessage("Unable to execute `(` XPath");
    }

    @Test
    void checkGetNodeNamesReturnsAllNames() {
        Path tempTestPomPath = getTempTestPomPath();
        Document document = readXml(tempTestPomPath);
        assertThat(getNodesNames(getNodesByXPath(document, "/project/dependencies/dependency/artifactId")))
                .containsExactlyInAnyOrder(
                        "artifactId",
                        "artifactId",
                        "artifactId"
                );
    }

    @Test
    void checkGetNodeChildrenReturnsAllChildren() {
        Path tempTestPomPath = getTempTestPomPath();
        Document document = readXml(tempTestPomPath);
        assertThat(getNodesNames(getNodeChildren(getNodeByXPath(document, "/project"))))
                .containsExactlyInAnyOrder(
                        "parent",
                        "modelVersion",
                        "artifactId",
                        "dependencies",
                        "groupId"
                );
    }

    @Test
    void checkGetNodeChildrenForNodeWithoutChildrenIsEmpty() {
        Path tempTestPomPath = getTempTestPomPath();
        Document document = readXml(tempTestPomPath);
        assertThat(getNodeChildren(getNodeByXPath(document, "/project/artifactId")))
                .isEmpty();
    }

}
