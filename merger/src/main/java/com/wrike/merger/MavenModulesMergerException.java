package com.wrike.merger;

/**
 * Custom {@link RuntimeException} for {@link MavenModulesMerger}.
 *
 * @author daniil.shylko on 26.08.2022
 */
public class MavenModulesMergerException extends RuntimeException {

    public MavenModulesMergerException(String message) {
        super(message);
    }

    public MavenModulesMergerException(String message, Throwable reason) {
        super(message, reason);
    }
}
