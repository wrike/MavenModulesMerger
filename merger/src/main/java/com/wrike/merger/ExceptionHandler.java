package com.wrike.merger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is created to be able to mock logic with System.exit().
 *
 * @author daniil.shylko on 30.08.2022
 * @see MavenModulesMerger
 */
class ExceptionHandler {

    private static final Logger LOG = LogManager.getLogger(ExceptionHandler.class);

    /**
     * Handler of exceptional cases. Writes error to console and exits with given code
     *
     * @param exception caught exception in {@link MavenModulesMerger}
     */
    void onException(Exception exception, ExitCode exitCode) {
        LOG.error("Merging failed.", exception);
        System.exit(exitCode.getExitCodeValue());
    }

}
