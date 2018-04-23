/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mute.runnables;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.TaskBase;
import io.github.nucleuspowered.nucleus.modules.mute.handler.MuteHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public class MuteTask implements TaskBase {

    private final MuteHandler muteHandler = getServiceUnchecked(MuteHandler.class);

    @Override
    public void accept(Task task) {
        Collection<Player> pl = Sponge.getServer().getOnlinePlayers().stream().filter(x -> muteHandler.isMutedCached(x))
                .collect(Collectors.toList());
        pl.stream().forEach(x -> Util.testForEndTimestamp(muteHandler.getPlayerMuteData(x), () -> muteHandler.unmutePlayer(x)));
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
