package com.wrike.merger.pom;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.wrike.merger.utils.TestFileUtils.getTempTestPomPath;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author daniil.shylko on 31.08.2022
 */
public class InputStreamPomParserTest {

    @Test
    void checkInputStreamPomParserCanBeCreated() throws IOException {
        try (InputStream inputStream = new FileInputStream(getTempTestPomPath().toFile())) {
            PomParser pomParser = new InputStreamPomParser(inputStream);
            assertThat(pomParser.getArtifactId())
                    .as("Check artifact id")
                    .isEqualTo("test_pom");
        }
    }

}
