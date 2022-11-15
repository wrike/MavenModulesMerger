package com.wrike.maven_modules_merger.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wrike.maven_modules_merger.utils.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Author: Daniil Shylko
 * Date: 17.08.2022
 */
public class TestFileUtils {

    private static final AtomicInteger COUNT = new AtomicInteger(0);
    private static final String TEMP_DIRECTORY_PREFIX = "maven_modules_merger_test";
    private static final Path TEMP_DIRECTORY;

    static {
        try {
            TEMP_DIRECTORY = Files.createTempDirectory(TEMP_DIRECTORY_PREFIX);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create test temp directory", e);
        }
    }

    private static Path getTempPomPath(String testPomName) {
        Path tempTestPomPath = TEMP_DIRECTORY.resolve(COUNT.incrementAndGet() + testPomName);
        try {
            Path pathToTestPom = Path.of(Objects.requireNonNull(
                    FileUtils.class.getResource(File.separator + testPomName)).toURI());
            Files.copy(pathToTestPom, tempTestPomPath);
        } catch (URISyntaxException | IOException e) {
            throw new IllegalStateException("Unable to create test temp pom file", e);
        }
        return tempTestPomPath;
    }

    public static Path getTempTestPomPath() {
        return getTempPomPath(TEST_POM);
    }

    public static Path getTempBrokenTestPomPath() {
        return getTempPomPath(BROKEN_TEST_POM);
    }

    public static Path getTempTestChildrenModulesPomPath() {
        return getTempPomPath(TEST_CHILDREN_MODULES_POM);
    }

    public static void checkFilesContentIsEqualIgnoringWhitespace(Path firstFile, Path secondFile) throws IOException {
        String firstFileContent = Files.readString(firstFile);
        String secondFileContent = Files.readString(secondFile);
        assertThat(firstFileContent).isEqualToIgnoringWhitespace(secondFileContent);
    }

    /**
     * Creates the temp directory with set of files from the given directory.
     * It creates the directory, increasing atomic {@link #COUNT} to avoid conflicts.
     * @param testProjectDirectory - one of {@link TestProjectDirectory} values which will be used to copy files from
     * @return path to craeted directory
     */
    public static Path createTempTestDirectory(TestProjectDirectory testProjectDirectory) {
        String directoryName = testProjectDirectory.getDirectoryName();
        Path tempTestDirectoryPath = TEMP_DIRECTORY.resolve(COUNT.incrementAndGet() + directoryName);
        try {
            Path pathToTestDirectory = Path.of(Objects.requireNonNull(
                    TestFileUtils.class.getResource(File.separator + directoryName)).toURI());
            FileUtils.copyDirectory(pathToTestDirectory.toFile(), tempTestDirectoryPath.toFile());
        } catch (URISyntaxException | IOException e) {
            throw new IllegalStateException("Unable to create test directory", e);
        }
        return tempTestDirectoryPath;
    }

    private static Set<Path> collectFilesInDirectory(Path directory) {
        try (Stream<Path> files = Files.walk(directory)) {
            return files
                    .map(directory::relativize)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to collect files of directory " + directory, e);
        }
    }

    public static void checkMergedDirectoryContainsOnlyContentOfOtherDirectories(
            Path mergedDirectory,
            List<Path> otherDirectories
    ) {
        Set<Path> allFilesToMerge = otherDirectories.stream()
                .map(TestFileUtils::collectFilesInDirectory)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        Set<Path> allMergedFiles = collectFilesInDirectory(mergedDirectory);

        assertThat(allFilesToMerge)
                .as("Check all files were merged")
                .isEqualTo(allMergedFiles);
    }

}
