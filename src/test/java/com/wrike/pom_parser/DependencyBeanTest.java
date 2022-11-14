package com.wrike.pom_parser;

import com.wrike.pom_parser.bean.Dependency;
import org.junit.jupiter.api.Test;

import static com.wrike.pom_parser.bean.DependencyScope.COMPILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Author: Daniil Shylko
 * Date: 18.08.2022
 */
public class DependencyBeanTest {

    private static final String TEST_FIELD = "test";

    @Test
    void checkDependencyCantBeCreatedWithoutGroupId() {
        assertThatThrownBy(
                () -> Dependency.builder()
                        .artifactId(TEST_FIELD)
                        .version(TEST_FIELD)
                        .build()
        ).isInstanceOf(NullPointerException.class)
                .hasMessage("groupId is marked non-null but is null");
    }

    @Test
    void checkDependencyCantBeCreatedWithoutArtifactId() {
        assertThatThrownBy(
                () -> Dependency.builder()
                        .groupId(TEST_FIELD)
                        .version(TEST_FIELD)
                        .build()
        ).isInstanceOf(NullPointerException.class)
                .hasMessage("artifactId is marked non-null but is null");
    }

    @Test
    void checkDependencyFieldsWithoutNonRequiredFields() {
        String groupId = "com.wrike";
        String artifactId = "artifact1";
        assertThat(
                Dependency.builder()
                        .groupId(groupId)
                        .artifactId(artifactId)
                        .build())
                .isEqualTo(
                        Dependency.builder()
                                .groupId(groupId)
                                .artifactId(artifactId)
                                .version(null)
                                .scope(COMPILE)
                                .build()
                );
    }

}
