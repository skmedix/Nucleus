/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jail.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.jail.JailModule;
import io.github.nucleuspowered.nucleus.modules.jail.config.JailConfig;
import io.github.nucleuspowered.nucleus.modules.jail.config.JailConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.jail.handlers.JailHandler;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.message.MessageChannelEvent;

public class ChatJailListener implements ListenerBase.Conditional {

    private final JailHandler handler = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(JailHandler.class);

    @Listener(order = Order.FIRST)
    public void onChat(MessageChannelEvent.Chat event) {
        Util.onPlayerSimulatedOrPlayer(event, this::onChat);
    }

    private void onChat(MessageChannelEvent.Chat event, Player player) {
        if (this.handler.checkJail(player, false)) {
            player.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("jail.muteonchat"));
            event.setCancelled(true);
        }
    }

    @Override public boolean shouldEnable() {
        return Nucleus.getNucleus().getConfigValue(JailModule.ID, JailConfigAdapter.class, JailConfig::isMuteOnJail).orElse(false);
    }

}
