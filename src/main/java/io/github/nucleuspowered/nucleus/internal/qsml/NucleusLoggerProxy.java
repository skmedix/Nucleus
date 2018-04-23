/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.qsml;

import org.slf4j.Logger;
import uk.co.drnaylor.quickstart.LoggerProxy;

public class NucleusLoggerProxy implements LoggerProxy {

    private final Logger logger;

    public NucleusLoggerProxy(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String message) {
        this.logger.info(message);
    }

    @Override
    public void warn(String message) {
        this.logger.warn(message);
    }

    @Override
    public void error(String message) {
        this.logger.error(message);
    }
}
