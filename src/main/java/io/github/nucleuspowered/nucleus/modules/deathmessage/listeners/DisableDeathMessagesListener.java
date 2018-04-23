/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.deathmessage.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.deathmessage.DeathMessageModule;
import io.github.nucleuspowered.nucleus.modules.deathmessage.config.DeathMessageConfigAdapter;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.filter.Getter;

public class DisableDeathMessagesListener implements ListenerBase.Conditional {

    @Listener(order = Order.BEFORE_POST)
    public void onDeath(DestructEntityEvent.Death event, @Getter("getTargetEntity") Living living) {
        if (living instanceof Player) {
            event.setMessageCancelled(true);
        }
    }

    @Override public boolean shouldEnable() {
        return Nucleus.getNucleus().getConfigValue(DeathMessageModule.ID, DeathMessageConfigAdapter.class, x -> !x.isEnableDeathMessages()).orElse(false);
    }

}
