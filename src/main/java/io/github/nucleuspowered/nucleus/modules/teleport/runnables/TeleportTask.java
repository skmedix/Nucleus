/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.runnables;

import io.github.nucleuspowered.nucleus.internal.TaskBase;
import io.github.nucleuspowered.nucleus.modules.teleport.handlers.TeleportHandler;
import org.spongepowered.api.scheduler.Task;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@SuppressWarnings("ALL")
public class TeleportTask implements TaskBase {

    private final TeleportHandler handler = getServiceUnchecked(TeleportHandler.class);

    @Override
    public void accept(Task task) {
        handler.clearExpired();
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public Duration interval() {
        return Duration.of(2, ChronoUnit.SECONDS);
    }
}
