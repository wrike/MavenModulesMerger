package com.wrike.merger.pom.bean;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import static com.wrike.merger.pom.bean.DependencyScope.COMPILE;

/**
 * Bean for storing dependency
 *
 * @author daniil.shylko on 17.08.2022
 */
@Data
@Builder
public class Dependency {

    @NonNull
    private String groupId;
    @NonNull
    private String artifactId;
    @EqualsAndHashCode.Exclude
    private String version;
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private DependencyScope scope = COMPILE;

}
