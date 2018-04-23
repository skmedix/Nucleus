/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jail.runnables;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.TaskBase;
import io.github.nucleuspowered.nucleus.modules.jail.handlers.JailHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public class JailTask implements TaskBase {

    private JailHandler jailHandler = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(JailHandler.class);

    @Override
    public void accept(Task task) {
        Collection<Player> pl = Sponge.getServer().getOnlinePlayers().stream().filter(x -> jailHandler.isPlayerJailedCached(x)).collect(Collectors.toList());
        pl.stream().forEach(x -> Util.testForEndTimestamp(jailHandler.getPlayerJailDataInternal(x), () -> jailHandler.unjailPlayer(x)));
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
