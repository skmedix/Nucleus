/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.misc.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@Permissions(supportsOthers = true)
@NoModifiers
@NonnullByDefault
@RegisterCommand("ping")
@EssentialsEquivalent(value = { "ping", "pong", "echo" }, isExact = false, notes = "Returns your latency, not your message.")
public class PingCommand extends AbstractCommand.SimpleTargetOtherPlayer {

    @Override protected CommandResult executeWithPlayer(CommandSource source, Player target, CommandContext args, boolean isSelf) {
        if (isSelf) {
            source.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.ping.current.self",
                String.valueOf(target.getConnection().getLatency())));
        } else {
            source.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.ping.current.other",
                target.getName(), String.valueOf(target.getConnection().getLatency())));
        }

        return CommandResult.success();
    }
}
