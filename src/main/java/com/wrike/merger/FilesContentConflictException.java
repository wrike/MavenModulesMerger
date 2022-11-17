package com.wrike.merger;

import java.nio.file.Path;

/**
 * Shows that conflict with files content occurs.
 *
 * @author daniil.shylko on 17.11.2022
 */
public class FilesContentConflictException extends Exception {

    public FilesContentConflictException(Path firstPath, Path secondPath) {
        super(String.format("Unable to copy file. There is a conflict between `%s` and `%s` files", firstPath, secondPath));
    }

}
