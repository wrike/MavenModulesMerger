package com.wrike.pom_parser.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.wrike.pom_parser.utils.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Author: Daniil Shylko
 * Date: 17.08.2022
 */
public class FileUtils {

    private static final AtomicInteger COUNT = new AtomicInteger(0);
    private static final String TEMP_DIRECTORY_PREFIX = "pom_parser_test";
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

}
