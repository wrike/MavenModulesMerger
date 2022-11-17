package com.wrike.merger.pom;

import com.wrike.merger.pom.bean.Dependency;
import com.wrike.merger.pom.bean.Dependency.DependencyBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.wrike.merger.pom.bean.DependencyScope.COMPILE;
import static com.wrike.merger.pom.bean.DependencyScope.getScopeByName;
import static com.wrike.merger.pom.utils.XmlUtils.*;

/**
 * Can read and write pom file.
 *
 * @author daniil.shylko on 17.08.2022
 */
public abstract class AbstractPomParser implements PomParser {

    public static final String POM_FILENAME = "pom.xml";

    private static final String PROJECT_PATH = "/project";
    private static final String PARENT_PATH = PROJECT_PATH + "/parent";
    private static final String PARENT_GROUP_ID_PATH = PARENT_PATH + "/groupId";
    private static final String DEPENDENCIES_PATH = PROJECT_PATH + "/dependencies";
    private static final String ARTIFACT_ID_PATH = PROJECT_PATH + "/artifactId";
    private static final String GROUP_ID_PATH = PROJECT_PATH + "/groupId";
    private static final String DEPENDENCY_PATH = DEPENDENCIES_PATH + "/dependency";
    private static final String DEPENDENCY_BY_GROUP_AND_ARTIFACT_ID_PATH =
            DEPENDENCY_PATH + "[groupId='%s'][artifactId='%s']";
    private static final String CHILDREN_MODULES_PATH = PROJECT_PATH + "/modules";
    private static final String CHILD_MODULE_PATH = CHILDREN_MODULES_PATH + "/module";
    private static final String CHILD_MODULE_PATH_BY_NAME = CHILDREN_MODULES_PATH + "/module[text()='%s']";

    private static final String ADDED_AUTOMATICALLY = "Added automatically via maven modules merger";

    private final Document document;

    public AbstractPomParser(Document document) {
        this.document = document;
    }

    @Override
    public void removeDependencyIfExists(Dependency dependency) {
        removeNodesByXPath(document, getDependencyXPath(dependency));
        if (getNodesByXPath(document, DEPENDENCY_PATH).isEmpty()) {
            removeAllDependencies();
        }
    }

    @Override
    public void removeAllDependencies() {
        removeNodesByXPath(document, DEPENDENCIES_PATH);
    }

    @Override
    public void setDependencies(Set<Dependency> dependencies) {
        removeAllDependencies();
        addDependenciesIfDoesNotExist(dependencies);
    }

    @Override
    public void addDependenciesIfDoesNotExist(Set<Dependency> dependencies) {
        dependencies.forEach(this::addDependencyIfDoesNotExist);
    }

    @Override
    public void addDependencyIfDoesNotExist(Dependency dependency) {
        if (getDependencyNode(dependency) != null) {
            return;
        }
        getDependenciesNode().appendChild(convertDependencyToXMLElement(dependency));
    }

    @Override
    public Set<Dependency> getAllDependencies() {
        return getNodesByXPath(document, DEPENDENCY_PATH).stream()
                .map(this::convertNodeToDependency)
                .collect(Collectors.toSet());
    }

    @Override
    public List<Node> getProjectChildren() {
        return getNodeChildren(getProjectNode());
    }

    @Override
    public String getArtifactId() {
        return getNodeByXPathTextContent(document, ARTIFACT_ID_PATH);
    }

    @Override
    public String getGroupId() {
        return getNodeByXPathTextContent(document, GROUP_ID_PATH);
    }

    @Override
    public String getParentGroupId() {
        return getNodeByXPathTextContent(document, PARENT_GROUP_ID_PATH);
    }

    @Override
    public String getEffectiveGroupId() {
        String groupId = getGroupId();
        if (groupId == null) {
            return getParentGroupId();
        }
        return groupId;
    }

    @Override
    public void addChildModuleIfDoesNotExist(String moduleName) {
        if (getChildModuleNode(moduleName) != null) {
            return;
        }
        getChildrenModulesNode().appendChild(getChildModuleXMLElement(moduleName));
    }

    @Override
    public Set<String> getChildrenModules() {
        return getNodesByXPath(document, CHILD_MODULE_PATH).stream()
                .map(Node::getTextContent)
                .collect(Collectors.toSet());
    }

    @Override
    public void writeToFile(Path filePath) {
        writePrettifiedXml(document, filePath);
    }

    private Element convertDependencyToXMLElement(Dependency dependency) {
        Element dependencyNode = document.createElement("dependency");
        Element groupId = document.createElement("groupId");
        groupId.appendChild(document.createTextNode(dependency.getGroupId()));
        Element artifactIdElement = document.createElement("artifactId");
        artifactIdElement.appendChild(document.createTextNode(dependency.getArtifactId()));
        dependencyNode.appendChild(document.createComment(ADDED_AUTOMATICALLY));
        dependencyNode.appendChild(groupId);
        dependencyNode.appendChild(artifactIdElement);
        if (dependency.getVersion() != null) {
            Element version = document.createElement("version");
            version.appendChild(document.createTextNode(dependency.getVersion()));
            dependencyNode.appendChild(version);
        }
        if (dependency.getScope() != null
                && !COMPILE.equals(dependency.getScope())) {
            Element scope = document.createElement("scope");
            scope.appendChild(document.createTextNode(dependency.getScope().getScopeName()));
            dependencyNode.appendChild(scope);
        }
        return dependencyNode;
    }

    private Element createDependenciesNode() {
        Element dependenciesNode = document.createElement("dependencies");
        getProjectNode().appendChild(dependenciesNode);
        return dependenciesNode;
    }

    private Dependency convertNodeToDependency(Node node) {
        List<Node> nodeChildren = getNodeChildren(node);
        Map<String, String> childrenValues = nodeChildren.stream()
                .collect(Collectors.toMap(Node::getNodeName, Node::getTextContent));
        DependencyBuilder dependencyBuilder = Dependency.builder()
                .groupId(childrenValues.get("groupId"))
                .artifactId(childrenValues.get("artifactId"))
                .version(childrenValues.get("version"));
        String scope = childrenValues.get("scope");
        if (scope != null) {
            dependencyBuilder
                    .scope(getScopeByName(scope)
                            .orElseThrow(() -> {
                                throw new PomParserException("Illegal scope: " + scope);
                            }));
        }
        return dependencyBuilder.build();
    }

    private Node getProjectNode() {
        return getNodeByXPath(document, PROJECT_PATH);
    }

    private Node getDependenciesNode() {
        Node dependenciesNode = getNodeByXPath(document, DEPENDENCIES_PATH);
        if (dependenciesNode == null) {
            getProjectNode().appendChild(createDependenciesNode());
            dependenciesNode = getNodeByXPath(document, DEPENDENCIES_PATH);
        }
        return dependenciesNode;
    }

    private String getDependencyXPath(Dependency dependency) {
        return String.format(DEPENDENCY_BY_GROUP_AND_ARTIFACT_ID_PATH,
                dependency.getGroupId(), dependency.getArtifactId());
    }

    private Node getDependencyNode(Dependency dependency) {
        return getNodeByXPath(document, getDependencyXPath(dependency));
    }

    private String getChildModuleXPath(String moduleName) {
        return String.format(CHILD_MODULE_PATH_BY_NAME, moduleName);
    }

    private Node getChildModuleNode(String moduleName) {
        return getNodeByXPath(document, getChildModuleXPath(moduleName));
    }

    private Node getChildrenModulesNode() {
        Node childrenModulesNode = getNodeByXPath(document, CHILDREN_MODULES_PATH);
        if (childrenModulesNode == null) {
            getProjectNode().appendChild(createChildrenModulesNode());
            childrenModulesNode = getNodeByXPath(document, CHILDREN_MODULES_PATH);
        }
        return childrenModulesNode;
    }

    private Element createChildrenModulesNode() {
        Element dependenciesNode = document.createElement("modules");
        getProjectNode().appendChild(dependenciesNode);
        return dependenciesNode;
    }

    private Element getChildModuleXMLElement(String moduleName) {
        Element childModuleNode = document.createElement("module");
        childModuleNode.appendChild(document.createTextNode(moduleName));
        return childModuleNode;
    }

}
