package com.wrike.merger;

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
        new MavenModulesMerger().merge(args);
    }

}
