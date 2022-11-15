package com.wrike.maven_modules_merger;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Contains exit codes, which merger can return
 *
 * @author daniil.shylko on 29.08.2022
 */
@Getter
@AllArgsConstructor
public enum ExitCode {

    MERGING_FAILED(3);

    private final int exitCodeValue;
}
