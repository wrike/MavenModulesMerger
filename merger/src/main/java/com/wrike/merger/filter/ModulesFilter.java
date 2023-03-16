package com.wrike.merger.filter;

import com.wrike.merger.MavenModulesMerger;

import java.nio.file.Path;

/**
 * Filter modules, which should be merged by {@link MavenModulesMerger}
 *
 * @author daniil.shylko on 15.11.2022
 */
public interface ModulesFilter {

    boolean moduleMatches(Path modulePath);

}
