/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.commandlogger.handlers;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.logging.AbstractLoggingHandler;
import io.github.nucleuspowered.nucleus.modules.commandlogger.config.CommandLoggerConfig;
import io.github.nucleuspowered.nucleus.modules.commandlogger.config.CommandLoggerConfigAdapter;

public class CommandLoggerHandler extends AbstractLoggingHandler implements Reloadable {

    private CommandLoggerConfig config;
    private final CommandLoggerConfigAdapter clca;

    public CommandLoggerHandler() {
        super("command", "cmds");
        this.clca = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(CommandLoggerConfigAdapter.class);
    }

    @Override
    public void onReload() throws Exception {
        this.config = this.clca.getNodeOrDefault();
        if (this.config.isLogToFile() && this.logger == null) {
            this.createLogger();
        } else if (!this.config.isLogToFile() && this.logger != null) {
            this.onShutdown();
        }
    }

    @Override
    protected boolean enabledLog() {
        if (this.config == null) {
            return false;
        }

        return this.config.isLogToFile();
    }
}
