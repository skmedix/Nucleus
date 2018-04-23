/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.afk.AFKModule;
import io.github.nucleuspowered.nucleus.modules.afk.config.AFKConfig;
import io.github.nucleuspowered.nucleus.modules.afk.config.AFKConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.afk.handlers.AFKHandler;
import org.spongepowered.api.entity.living.player.Player;

import java.util.function.Predicate;

abstract class AbstractAFKListener implements ListenerBase {

    private final AFKHandler handler = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(AFKHandler.class);

    final void update(Player player) {
        this.handler.stageUserActivityUpdate(player);
    }

    final boolean getTriggerConfigEntry(Predicate<AFKConfig.Triggers> triggersPredicate) {
        return Nucleus.getNucleus().getConfigValue(AFKModule.ID, AFKConfigAdapter.class, x -> triggersPredicate.test(x.getTriggers())).orElse(false);
    }
}
