package com.wrike.merger.pom;

import com.wrike.merger.pom.bean.Dependency;
import com.wrike.merger.pom.bean.Parent;
import org.w3c.dom.Node;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Pom parser interface, contains main methods for working with pom files.
 *
 * @author daniil.shylko on 31.08.2022
 */
public interface PomParser {

    void removeDependencyIfExists(Dependency dependency);

    void removeAllDependencies();

    void setDependencies(Set<Dependency> dependencies);

    void addDependenciesIfDoesNotExist(Set<Dependency> dependencies);

    void addDependencyIfDoesNotExist(Dependency dependency);

    Set<Dependency> getAllDependencies();

    List<Node> getProjectChildren();

    String getArtifactId();

    String getGroupId();

    String getVersion();

    String getParentGroupId();

    String getEffectiveGroupId();

    void addChildModuleIfDoesNotExist(String moduleName);

    Set<String> getChildrenModules();

    void setParent(Parent parent);

    void writeToFile(Path filePath);

}
