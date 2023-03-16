package com.wrike.merger.pom.bean;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * Bean for storing info about parent
 *
 * @author daniil.shylko on 16.03.2023
 */
@Data
@Builder
public class Parent {

    @NonNull
    private String groupId;
    @NonNull
    private String artifactId;
    @NonNull
    private String version;

}
