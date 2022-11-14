package com.wrike.pom_parser;

import org.junit.jupiter.api.Test;

import static com.wrike.pom_parser.utils.Constants.INVALID_PATH;
import static com.wrike.pom_parser.utils.FileUtils.getTempBrokenTestPomPath;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Author: Daniil Shylko
 * Date: 18.08.2022
 */
public class PomParserExceptionTest {

    @Test
    void checkCollectAllPomsThrowsExceptionForInvalidPath() {
        assertThatThrownBy(
                () -> FilePomParser.collectAllPomFiles(INVALID_PATH)
        ).isInstanceOf(PomParserException.class)
                .hasMessage(String.format("Unable to collect pom files from `%s` root", INVALID_PATH));
    }

    @Test
    void checkIllegalScopeException() {
        FilePomParser pomParser = new FilePomParser(getTempBrokenTestPomPath());
        assertThatThrownBy(
                pomParser::getAllDependencies
        ).isInstanceOf(PomParserException.class)
                .hasMessage("Illegal scope: I_am_the_broken_scope");

    }

}
