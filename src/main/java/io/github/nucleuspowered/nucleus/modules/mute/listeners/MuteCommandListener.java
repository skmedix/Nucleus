/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mute.listeners;

import com.google.common.collect.Sets;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.mute.config.MuteConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.mute.data.MuteData;
import io.github.nucleuspowered.nucleus.modules.mute.handler.MuteHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MuteCommandListener implements ListenerBase.Conditional {

    private final List<String> blockedCommands = new ArrayList<>();
    private final MuteHandler handler = getServiceUnchecked(MuteHandler.class);

    /**
     * Checks for blocked commands when muted.
     *
     * @param event The {@link SendCommandEvent} containing the command.
     * @param player The {@link Player} who executed the command.
     */
    @Listener(order = Order.FIRST)
    public void onPlayerSendCommand(SendCommandEvent event, @Root Player player) {
        if (!this.handler.isMutedCached(player)) {
            return;
        }

        String command = event.getCommand().toLowerCase();
        Optional<? extends CommandMapping> oc = Sponge.getCommandManager().get(command, player);
        Set<String> cmd;

        // If the command exists, then get all aliases.
        cmd = oc.map(commandMapping -> commandMapping.getAllAliases().stream().map(String::toLowerCase).collect(Collectors.toSet()))
                .orElseGet(() -> Sets.newHashSet(command));

        // If the command is in the list, block it.
        if (this.blockedCommands.stream().map(String::toLowerCase).anyMatch(cmd::contains)) {
            MuteData muteData = this.handler.getPlayerMuteData(player).orElse(null);
            if (muteData == null || muteData.expired()) {
                this.handler.unmutePlayer(player);
            } else {
                this.handler.onMute(muteData, player);
                MessageChannel.TO_CONSOLE.send(Text.builder().append(Text.of(player.getName() + " ("))
                        .append(getMessage("standard.muted"))
                        .append(Text.of("): ")).append(Text.of("/" + event.getCommand() + " " + event.getArguments())).build());
                event.setCancelled(true);
            }
        }
    }

    // will also act as the reloadable.
    @Override public boolean shouldEnable() {
        this.blockedCommands.clear();
        this.blockedCommands.addAll(getServiceUnchecked(MuteConfigAdapter.class).getNodeOrDefault().getBlockedCommands());
        return !this.blockedCommands.isEmpty();
    }
}
