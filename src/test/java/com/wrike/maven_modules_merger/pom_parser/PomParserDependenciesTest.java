package com.wrike.maven_modules_merger.pom_parser;

import com.wrike.maven_modules_merger.pom_parser.bean.Dependency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.List;
import java.util.Set;

import static com.wrike.maven_modules_merger.pom_parser.bean.DependencyScope.RUNTIME;
import static com.wrike.maven_modules_merger.pom_parser.bean.DependencyScope.TEST;
import static com.wrike.maven_modules_merger.pom_parser.utils.XmlUtils.*;
import static com.wrike.maven_modules_merger.utils.Constants.GROUP_ID;
import static com.wrike.maven_modules_merger.utils.Constants.TEST_DEPENDENCIES_NAMES;
import static com.wrike.maven_modules_merger.utils.TestFileUtils.getTempTestPomPath;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author daniil.shylko on 17.08.2022
 */
public class PomParserDependenciesTest {

    private static final List<Dependency> DEPENDENCIES = List.of(
            Dependency.builder()
                    .groupId(GROUP_ID)
                    .artifactId(TEST_DEPENDENCIES_NAMES.get(0))
                    .build(),
            Dependency.builder()
                    .groupId(GROUP_ID)
                    .artifactId(TEST_DEPENDENCIES_NAMES.get(1))
                    .build(),
            Dependency.builder()
                    .groupId(GROUP_ID)
                    .artifactId(TEST_DEPENDENCIES_NAMES.get(2))
                    .version("1.0-SNAPSHOT")
                    .scope(TEST)
                    .build()
    );
    private static final Dependency TEST_UTILS_DEPENDENCY = DEPENDENCIES.get(0);
    private static final Dependency DEPENDENCY_WITH_CUSTOM_SCOPE = DEPENDENCIES.get(2);
    private static final Dependency NEW_DEPENDENCY = Dependency.builder()
            .groupId(GROUP_ID)
            .artifactId("new_artifact1")
            .version("1.0-SNAPSHOT")
            .build();

    private FilePomParser pomParser;

    @BeforeEach
    void prepare() {
        pomParser = new FilePomParser(getTempTestPomPath());
    }

    @Test
    void checkDependencyCanBeRemoved() {
        pomParser.removeDependencyIfExists(TEST_UTILS_DEPENDENCY);
        pomParser.writeToOriginFile();
        Document actualDocument = readXml(pomParser.getOriginPath());
        checkDependencyIsAbsent(actualDocument, TEST_UTILS_DEPENDENCY);
        checkDependenciesSize(actualDocument, 2);
    }

    @Test
    void checkDependencyWithCustomScopeCanBeRemoved() {
        pomParser.removeDependencyIfExists(DEPENDENCY_WITH_CUSTOM_SCOPE);
        pomParser.writeToOriginFile();
        Document actualDocument = readXml(pomParser.getOriginPath());
        checkDependencyIsAbsent(actualDocument, DEPENDENCY_WITH_CUSTOM_SCOPE);
        checkDependenciesSize(actualDocument, 2);
    }

    @Test
    void checkRemovingOfNonExistentDependencyIsIgnored() {
        pomParser.removeDependencyIfExists(TEST_UTILS_DEPENDENCY);
        pomParser.removeDependencyIfExists(TEST_UTILS_DEPENDENCY);
        pomParser.writeToOriginFile();
        Document actualDocument = readXml(pomParser.getOriginPath());
        checkDependencyIsAbsent(actualDocument, TEST_UTILS_DEPENDENCY);
        checkDependenciesSize(actualDocument, 2);
    }

    @Test
    void checkDependencyCanBeAdded() {
        Dependency dependencyToAdd = NEW_DEPENDENCY;
        pomParser.addDependencyIfDoesNotExist(dependencyToAdd);
        pomParser.writeToOriginFile();
        Document actualDocument = readXml(pomParser.getOriginPath());
        checkDependencyExists(actualDocument, dependencyToAdd);
        checkDependenciesSize(actualDocument, 4);
    }

    @Test
    void checkDependencyWithCustomScopeCanBeAdded() {
        Dependency dependencyToAdd = NEW_DEPENDENCY;
        dependencyToAdd.setScope(RUNTIME);
        pomParser.addDependencyIfDoesNotExist(dependencyToAdd);
        pomParser.writeToOriginFile();
        Document actualDocument = readXml(pomParser.getOriginPath());
        checkDependencyWithScopeExists(actualDocument, dependencyToAdd);
        checkDependenciesSize(actualDocument, 4);
    }

    @Test
    void checkAddingOfExistentDependencyIsIgnored() {
        Dependency dependencyToAdd = NEW_DEPENDENCY;
        pomParser.addDependencyIfDoesNotExist(dependencyToAdd);
        pomParser.addDependencyIfDoesNotExist(dependencyToAdd);
        pomParser.writeToOriginFile();
        Document actualDocument = readXml(pomParser.getOriginPath());
        checkDependencyExists(actualDocument, dependencyToAdd);
        checkDependenciesSize(actualDocument, 4);
    }

    @Test
    void checkTwoDifferentDependenciesCanBeAdded() {
        Dependency dependencyToAdd = NEW_DEPENDENCY;
        Dependency dependencyToAdd2 = Dependency.builder()
                .groupId(GROUP_ID)
                .artifactId("new_artifact2")
                .version("1.0-SNAPSHOT")
                .build();
        pomParser.addDependenciesIfDoesNotExist(Set.of(dependencyToAdd, dependencyToAdd2));
        pomParser.writeToOriginFile();
        Document actualDocument = readXml(pomParser.getOriginPath());
        checkDependencyExists(actualDocument, dependencyToAdd);
        checkDependencyExists(actualDocument, dependencyToAdd2);
        checkDependenciesSize(actualDocument, 5);
    }

    @Test
    void checkAllDependenciesCanBeRemovedOneByOne() {
        DEPENDENCIES.forEach(pomParser::removeDependencyIfExists);
        pomParser.writeToOriginFile();
        Document actualDocument = readXml(pomParser.getOriginPath());
        checkDependenciesParentNodeIsAbsent(actualDocument);
    }

    @Test
    void checkAllDependenciesCanBeRemoved() {
        pomParser.removeAllDependencies();
        pomParser.writeToOriginFile();
        Document actualDocument = readXml(pomParser.getOriginPath());
        checkDependenciesParentNodeIsAbsent(actualDocument);
    }

    @Test
    void checkAllDependenciesCanBeRemovedOneByOneAndThenAdded() {
        Dependency dependencyToAdd = NEW_DEPENDENCY;
        DEPENDENCIES.forEach(pomParser::removeDependencyIfExists);
        pomParser.addDependencyIfDoesNotExist(dependencyToAdd);
        pomParser.writeToOriginFile();
        Document actualDocument = readXml(pomParser.getOriginPath());
        checkDependencyExists(actualDocument, dependencyToAdd);
        checkDependenciesSize(actualDocument, 1);
    }

    @Test
    void checkAllDependenciesCanBeRemovedAndThenAdded() {
        Dependency dependencyToAdd = NEW_DEPENDENCY;
        pomParser.removeAllDependencies();
        pomParser.addDependencyIfDoesNotExist(dependencyToAdd);
        pomParser.writeToOriginFile();
        Document actualDocument = readXml(pomParser.getOriginPath());
        checkDependencyExists(actualDocument, dependencyToAdd);
        checkDependenciesSize(actualDocument, 1);
    }

    @Test
    void checkDependenciesCanBeSet() {
        Dependency dependencyToSet = NEW_DEPENDENCY;
        pomParser.setDependencies(Set.of(dependencyToSet));
        pomParser.writeToOriginFile();
        Document actualDocument = readXml(pomParser.getOriginPath());
        checkDependencyExists(actualDocument, dependencyToSet);
        checkDependenciesSize(actualDocument, 1);
    }

    @Test
    void checkGetAllDependenciesReturnsAllDependencies() {
        assertThat(pomParser.getAllDependencies())
                .containsExactlyInAnyOrderElementsOf(DEPENDENCIES);
    }

    private List<Node> getDependency(Document document, Dependency dependency) {
        return getNodesByXPath(document, String.format(
                "/project/dependencies/dependency[groupId='%s'][artifactId='%s']",
                dependency.getGroupId(),
                dependency.getArtifactId()
        ));
    }

    private List<Node> getDependencyWithScope(Document document, Dependency dependency) {
        return getNodesByXPath(document, String.format(
                "/project/dependencies/dependency[groupId='%s'][artifactId='%s'][scope='%s']",
                dependency.getGroupId(),
                dependency.getArtifactId(),
                dependency.getScope().getScopeName()
        ));
    }

    private void checkDependencyExists(Document document, Dependency dependency) {
        assertThat(getDependency(document, dependency)).hasSize(1);
    }

    private void checkDependencyWithScopeExists(Document document, Dependency dependency) {
        assertThat(getDependencyWithScope(document, dependency)).hasSize(1);
    }

    private void checkDependencyIsAbsent(Document document, Dependency dependency) {
        assertThat(getDependency(document, dependency)).isEmpty();
    }

    private void checkDependenciesParentNodeIsAbsent(Document document) {
        Node dependencies = getNodeByXPath(document, "/project/dependencies");
        assertThat(dependencies).isNull();
    }

    private void checkDependenciesSize(Document document, int size) {
        List<Node> dependencies = getNodesByXPath(document, "/project/dependencies/dependency");
        assertThat(dependencies).hasSize(size);
    }

}
