/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.misc.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@Permissions(supportsOthers = true)
@RegisterCommand("heal")
@EssentialsEquivalent("heal")
@NonnullByDefault
public class HealCommand extends AbstractCommand.SimpleTargetOtherPlayer {

    @Override protected CommandResult executeWithPlayer(CommandSource src, Player pl, CommandContext args, boolean isSelf) {
        if (pl.offer(Keys.HEALTH, pl.get(Keys.MAX_HEALTH).get()).isSuccessful()) {
            pl.offer(Keys.FIRE_TICKS, 0);
            pl.sendMessages(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.heal.success.self"));
            if (!isSelf) {
                src.sendMessages(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.heal.success.other", pl.getName()));
            }

            return CommandResult.success();
        } else {
            src.sendMessages(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.heal.error"));
            return CommandResult.empty();
        }
    }
}
