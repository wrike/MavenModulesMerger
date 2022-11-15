package com.wrike.maven_modules_merger.pom_parser;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.List;
import java.util.Set;

import static com.wrike.maven_modules_merger.pom_parser.utils.XmlUtils.getNodesByXPath;
import static com.wrike.maven_modules_merger.pom_parser.utils.XmlUtils.readXml;
import static com.wrike.maven_modules_merger.utils.TestFileUtils.getTempTestChildrenModulesPomPath;
import static com.wrike.maven_modules_merger.utils.TestFileUtils.getTempTestPomPath;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author daniil.shylko on 17.08.2022
 */
public class PomParserChildrenModulesTest {

    private static final String NEW_CHILD_MODULE = "new_child_module";

    private FilePomParser pomParser;

    @Test
    void checkChildModuleCanBeAdded() {
        pomParser = new FilePomParser(getTempTestChildrenModulesPomPath());
        pomParser.addChildModuleIfDoesNotExist(NEW_CHILD_MODULE);
        pomParser.writeToOriginFile();
        Document actualDocument = readXml(pomParser.getOriginPath());
        checkChildModuleExists(actualDocument, NEW_CHILD_MODULE);
        checkChildrenModulesSize(actualDocument, 2);
    }

    @Test
    void checkChildModuleCanBeAddedToPomWithoutChildrenModules() {
        pomParser = new FilePomParser(getTempTestPomPath());
        pomParser.addChildModuleIfDoesNotExist(NEW_CHILD_MODULE);
        pomParser.writeToOriginFile();
        Document actualDocument = readXml(pomParser.getOriginPath());
        checkChildModuleExists(actualDocument, NEW_CHILD_MODULE);
        checkChildrenModulesSize(actualDocument, 1);
    }

    @Test
    void checkAddingOfExistentChildModuleIsIgnored() {
        pomParser = new FilePomParser(getTempTestChildrenModulesPomPath());
        pomParser.addChildModuleIfDoesNotExist(NEW_CHILD_MODULE);
        pomParser.addChildModuleIfDoesNotExist(NEW_CHILD_MODULE);
        pomParser.writeToOriginFile();
        Document actualDocument = readXml(pomParser.getOriginPath());
        checkChildModuleExists(actualDocument, NEW_CHILD_MODULE);
        checkChildrenModulesSize(actualDocument, 2);
    }

    @Test
    void checkTwoDifferentChildrenModulesCanBeAdded() {
        String secondChildModule = "second_child_module";
        pomParser = new FilePomParser(getTempTestChildrenModulesPomPath());
        pomParser.addChildModuleIfDoesNotExist(NEW_CHILD_MODULE);
        pomParser.addChildModuleIfDoesNotExist(secondChildModule);
        pomParser.writeToOriginFile();
        Document actualDocument = readXml(pomParser.getOriginPath());
        checkChildModuleExists(actualDocument, NEW_CHILD_MODULE);
        checkChildModuleExists(actualDocument, secondChildModule);
        checkChildrenModulesSize(actualDocument, 3);
    }

    @Test
    void checkGettingOfAllChildrenModules() {
        pomParser = new FilePomParser(getTempTestChildrenModulesPomPath());
        assertThat(pomParser.getChildrenModules())
                .as("Check children modules")
                .isEqualTo(Set.of("child_module"));
    }

    private List<Node> getChildrenModules(Document document, String moduleName) {
        return getNodesByXPath(document, String.format("/project/modules/module[text()='%s']", moduleName));
    }

    private void checkChildModuleExists(Document document, String moduleName) {
        assertThat(getChildrenModules(document, moduleName)).hasSize(1);
    }

    private void checkChildrenModulesSize(Document document, int size) {
        List<Node> dependencies = getNodesByXPath(document, "/project/modules/module");
        assertThat(dependencies).hasSize(size);
    }

}
