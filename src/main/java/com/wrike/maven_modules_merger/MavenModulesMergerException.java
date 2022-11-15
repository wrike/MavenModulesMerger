package com.wrike.maven_modules_merger;

/**
 * Author: Daniil Shylko
 * Date: 26.08.2022
 * <p>
 *     Custom {@link RuntimeException} for {@link MavenModulesMerger}.
 * </p>
 */
public class MavenModulesMergerException extends RuntimeException {

    public MavenModulesMergerException(String message) {
        super(message);
    }

    public MavenModulesMergerException(String message, Throwable reason) {
        super(message, reason);
    }
}
