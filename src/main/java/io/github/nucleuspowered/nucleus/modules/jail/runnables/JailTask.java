/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jail.runnables;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.TaskBase;
import io.github.nucleuspowered.nucleus.modules.jail.handlers.JailHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.scheduler.Task;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@SuppressWarnings("ALL")
public class JailTask implements TaskBase {

    private JailHandler jailHandler = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(JailHandler.class);

    @Override
    public void accept(Task task) {
        Sponge.getServer()
                .getOnlinePlayers()
                .stream()
                .filter(x -> this.jailHandler.isPlayerJailedCached(x))
                .filter(x -> this.jailHandler.getPlayerJailDataInternal(x).map(y -> y.expired()).orElse(false))
                .forEach(x -> this.jailHandler.unjailPlayer(x, Cause.of(EventContext.empty(), Nucleus.getNucleus())));
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public Duration interval() {
        return Duration.of(1, ChronoUnit.SECONDS);
    }

}
