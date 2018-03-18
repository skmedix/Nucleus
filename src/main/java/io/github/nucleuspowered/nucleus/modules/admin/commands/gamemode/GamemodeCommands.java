/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.admin.commands.gamemode;

import io.github.nucleuspowered.nucleus.internal.annotations.command.PermissionsFrom;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Scan;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@Scan
public class GamemodeCommands {

    @NonnullByDefault
    @RegisterCommand({"gms", "survival"})
    @EssentialsEquivalent({"gms", "survival"})
    @PermissionsFrom(value = GamemodeCommand.class, requiresSuffix = "modes.survival")
    public static class Survival extends GamemodeBase<Player> {

        @Override
        protected CommandResult executeCommand(Player src, CommandContext args) throws Exception {
            return baseCommand(src, src, GameModes.SURVIVAL);
        }
    }

    @NonnullByDefault
    @RegisterCommand({"gmc", "creative"})
    @EssentialsEquivalent({"gmc", "creative"})
    @PermissionsFrom(value = GamemodeCommand.class, requiresSuffix = "modes.creative")
    public static class Creative extends GamemodeBase<Player> {

        @Override
        protected CommandResult executeCommand(Player src, CommandContext args) throws Exception {
            return baseCommand(src, src, GameModes.CREATIVE);
        }
    }

    @NonnullByDefault
    @RegisterCommand({"gma", "adventure"})
    @EssentialsEquivalent({"gma", "adventure"})
    @PermissionsFrom(value = GamemodeCommand.class, requiresSuffix = "modes.adventure")
    public static class Adventure extends GamemodeBase<Player> {

        @Override
        protected CommandResult executeCommand(Player src, CommandContext args) throws Exception {
            return baseCommand(src, src, GameModes.ADVENTURE);
        }
    }

    @NonnullByDefault
    @RegisterCommand("gmsp")
    @PermissionsFrom(value = GamemodeCommand.class, requiresSuffix = "modes.spectator")
    public static class Spectator extends GamemodeBase<Player> {

        @Override
        protected CommandResult executeCommand(Player src, CommandContext args) throws Exception {
            return baseCommand(src, src, GameModes.SPECTATOR);
        }
    }
}
