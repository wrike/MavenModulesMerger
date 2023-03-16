package com.wrike.merger;

import static com.wrike.merger.filter.ExistingFileModulesFilter.filterByAllureProperties;

/**
 * Starts {@link MavenModulesMerger} with a given arguments.
 * <p>
 * Can be directly called from CI and other tools.
 * </p>
 *
 * @author daniil.shylko on 30.08.2022
 */
public class MavenModulesMergerStarter {

    public static void main(String[] args) {
        new MavenModulesMerger(filterByAllureProperties()).merge(args);
    }

}
