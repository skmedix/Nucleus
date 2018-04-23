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
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@Permissions(suggestedLevel = SuggestedLevel.USER)
@RegisterCommand("suicide")
@EssentialsEquivalent("suicide")
@NonnullByDefault
public class SuicideCommand extends AbstractCommand<Player> {

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) {
        GameMode gm = src.gameMode().getDirect().orElse(src.gameMode().getDefault());
        if (gm != GameModes.SURVIVAL && gm != GameModes.NOT_SET) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.suicide.wronggm"));
            return CommandResult.empty();
        }

        src.offer(Keys.HEALTH, 0d);
        return CommandResult.success();
    }
}
