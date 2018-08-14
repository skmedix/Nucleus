/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk.listeners;

import io.github.nucleuspowered.nucleus.api.events.NucleusAFKEvent;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.traits.InternalServiceManagerTrait;
import io.github.nucleuspowered.nucleus.internal.traits.PermissionTrait;
import io.github.nucleuspowered.nucleus.modules.afk.commands.AFKCommand;
import io.github.nucleuspowered.nucleus.modules.afk.config.AFKConfigAdapter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;

public class AFKSpectatorListener implements ListenerBase.Conditional, InternalServiceManagerTrait, PermissionTrait {

    private final String NOTIFY = getPermissionHandlerFor(AFKCommand.class).getPermissionWithSuffix("notify");

    @Listener
    public void onAfk(NucleusAFKEvent event, @Getter("getTargetEntity") Player player) {
        if (player.gameMode().get().equals(GameModes.SPECTATOR)) {
            if (event.getChannel() == MessageChannel.TO_ALL) {
                event.setChannel(MessageChannel.permission(NOTIFY));
                event.setMessage(Text.of(TextColors.YELLOW, "[Spectator] ", event.getMessage()));
            }
        }
    }

    @Listener(order = Order.FIRST)
    public void onAfk(NucleusAFKEvent.Kick event, @Getter("getTargetEntity") Player player) {
        if (player.gameMode().get().equals(GameModes.SPECTATOR)) {
            event.setCancelled(true);
        }
    }

    @Override
    public boolean shouldEnable() {
        return getServiceUnchecked(AFKConfigAdapter.class).getNodeOrDefault().isDisableInSpectatorMode();
    }
}
