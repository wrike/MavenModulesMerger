package com.wrike.maven_modules_merger.modules_filter;

import com.wrike.maven_modules_merger.MavenModulesMerger;

import java.nio.file.Path;

/**
 * Filter modules, which should be merged by {@link MavenModulesMerger}
 *
 * @author daniil.shylko on 15.11.2022
 */
public interface ModulesFilter {

    boolean moduleMatches(Path modulePath);

}
