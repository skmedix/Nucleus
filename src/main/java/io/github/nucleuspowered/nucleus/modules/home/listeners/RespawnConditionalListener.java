/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.home.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.service.NucleusHomeService;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.home.HomeModule;
import io.github.nucleuspowered.nucleus.modules.home.config.HomeConfig;
import io.github.nucleuspowered.nucleus.modules.home.config.HomeConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.home.datamodules.HomeUserDataModule;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.Getter;

public class RespawnConditionalListener implements ListenerBase.Conditional {

    @Listener
    public void onRespawn(final RespawnPlayerEvent event, @Getter("getTargetEntity") final Player player) {
        Nucleus.getNucleus().getUserDataManager().getUnchecked(player)
            .get(HomeUserDataModule.class)
            .getHome(NucleusHomeService.DEFAULT_HOME_NAME)
            .ifPresent(x -> x.getTransform().ifPresent(event::setToTransform));
    }

    @Override public boolean shouldEnable() {
        return Nucleus.getNucleus().getConfigValue(HomeModule.ID, HomeConfigAdapter.class, HomeConfig::isRespawnAtHome).orElse(false);
    }


}
