/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mute.runnables;

import io.github.nucleuspowered.nucleus.internal.TaskBase;
import io.github.nucleuspowered.nucleus.modules.mute.handler.MuteHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@SuppressWarnings("ALL")
public class MuteTask implements TaskBase {

    private final MuteHandler muteHandler = getServiceUnchecked(MuteHandler.class);

    @Override
    public void accept(Task task) {
        Sponge.getServer()
                .getOnlinePlayers()
                .stream()
                .filter(x -> this.muteHandler.isMutedCached(x))
                .filter(x -> this.muteHandler.getPlayerMuteData(x).map(y -> y.expired()).orElse(false))
                .forEach(this.muteHandler::unmutePlayer);
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
