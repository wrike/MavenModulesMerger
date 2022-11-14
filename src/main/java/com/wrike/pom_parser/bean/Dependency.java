package com.wrike.pom_parser.bean;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import static com.wrike.pom_parser.bean.DependencyScope.COMPILE;

/**
 * Author: Daniil Shylko
 * Date: 17.08.2022
 */
@Data
@Builder
public class Dependency {

    @NonNull
    private String groupId;
    @NonNull
    private String artifactId;
    private String version;
    @Builder.Default
    private DependencyScope scope = COMPILE;

}
