package com.wrike.merger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides methods for working with files
 *
 * @author daniil.shylko on 17.11.2022
 */
public class FileUtils {

    /**
     * Copies directory with checking for conflicts in files content
     * <p>
     * For file coping and resolving possible conflicts in files content it uses
     * {@link #copyFileWithCheckingConflict(Path, Path)} method
     * </p>
     *
     * @param source source directory
     * @param target target directory
     * @throws IOException                   if error occurs while copying files
     * @throws FilesContentConflictException if files have content conflict
     */
    public static void copyDirectoryWithCheckingConflicts(Path source, Path target) throws IOException, FilesContentConflictException {
        Files.createDirectories(target);
        try (Stream<Path> files = Files.walk(source)) {
            List<Path> filesList = files.collect(Collectors.toList());
            for (Path path : filesList) {
                Path newFileLocation = target.resolve(source.relativize(path));
                copyFileWithCheckingConflict(path, newFileLocation);
            }
        }
    }

    /**
     * It copies file to new location and checks for possible conflicts.
     *
     * @param file            source file
     * @param newFileLocation place, where copy of file will be stored
     * @throws IOException                   if copying failed
     * @throws FilesContentConflictException if files have content conflict
     */
    private static void copyFileWithCheckingConflict(Path file, Path newFileLocation) throws IOException, FilesContentConflictException {
        if (Files.exists(newFileLocation)) {
            if (Files.isRegularFile(newFileLocation)
                    && !org.apache.commons.io.FileUtils.contentEquals(file.toFile(), newFileLocation.toFile())) {
                throw new FilesContentConflictException(file, newFileLocation);
            }
        } else {
            Files.copy(file, newFileLocation);
        }
    }

}
