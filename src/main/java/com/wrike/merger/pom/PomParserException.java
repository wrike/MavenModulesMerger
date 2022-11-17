package com.wrike.merger.pom;

/**
 * The custom exception for {@link PomParser}.
 *
 * @author daniil.shylko on 18.08.2022
 */
public class PomParserException extends RuntimeException {

    public PomParserException(String message) {
        super(message);
    }

    public PomParserException(String message, Throwable reason) {
        super(message, reason);
    }

}
