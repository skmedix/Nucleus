/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.admin.commands.gamemode;

import io.github.nucleuspowered.nucleus.internal.annotations.command.PermissionsFrom;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
@RegisterCommand("gmt")
@EssentialsEquivalent("gmt")
@PermissionsFrom(value = GamemodeCommand.class, requiresSuffix = {"modes.survival", "modes.creative"})
public class GamemodeToggleCommand extends GamemodeBase<Player> {

    @Override
    protected CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        GameMode mode = src.get(Keys.GAME_MODE).orElse(GameModes.SURVIVAL);
        if (mode.equals(GameModes.SURVIVAL) || mode.equals(GameModes.NOT_SET)) {
            mode = GameModes.CREATIVE;
        } else {
            mode = GameModes.SURVIVAL;
        }

        return baseCommand(src, src, mode);
    }
}
