/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.commandlogger.runnables;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.TaskBase;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.commandlogger.config.CommandLoggerConfig;
import io.github.nucleuspowered.nucleus.modules.commandlogger.config.CommandLoggerConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.commandlogger.handlers.CommandLoggerHandler;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@NonnullByDefault
public class CommandLoggerRunnable implements TaskBase, Reloadable {

    private final CommandLoggerHandler handler = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(CommandLoggerHandler.class);
    private CommandLoggerConfig config = new CommandLoggerConfig();

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public Duration interval() {
        return Duration.of(1, ChronoUnit.SECONDS);
    }

    @Override
    public void accept(Task task) {
        if (Sponge.getGame().getState() == GameState.SERVER_STOPPED) {
            return;
        }

        if (this.config.isLogToFile()) {
            this.handler.onTick();
        }
    }

    @Override
    public void onReload() {
        this.config = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(CommandLoggerConfigAdapter.class).getNodeOrDefault();
    }
}
