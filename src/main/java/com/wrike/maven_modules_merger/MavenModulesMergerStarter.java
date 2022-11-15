package com.wrike.maven_modules_merger;

import static com.wrike.maven_modules_merger.modules_filter.ExistingFileModulesFilter.filterByAllureProperties;

/**
 * Author: Daniil Shylko
 * Date: 30.08.2022
 * <p>
 * Starts {@link MavenModulesMerger} with a given arguments.
 * Can be directly called from CI and other tools.
 * </p>
 */
public class MavenModulesMergerStarter {

    public static void main(String[] args) {
        new MavenModulesMerger(filterByAllureProperties()).merge(args);
    }

}
