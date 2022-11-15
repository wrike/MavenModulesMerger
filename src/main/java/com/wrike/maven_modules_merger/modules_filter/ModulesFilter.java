package com.wrike.maven_modules_merger.modules_filter;

import com.wrike.maven_modules_merger.MavenModulesMerger;

import java.nio.file.Path;

/**
 * Author: Daniil Shylko
 * Date: 15.11.2022
 * Filter modules, which should be merged by {@link MavenModulesMerger}
 */
public interface ModulesFilter {

    boolean moduleMatches(Path modulePath);

}
