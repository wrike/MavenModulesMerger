package com.wrike.pom_parser;

import com.wrike.pom_parser.bean.Dependency;
import org.w3c.dom.Node;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Author: Daniil Shylko
 * Date: 31.08.2022
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

    void addChildModuleIfDoesNotExist(String moduleName);

    Set<String> getChildrenModules();

    void writeToFile(Path filePath);

}
