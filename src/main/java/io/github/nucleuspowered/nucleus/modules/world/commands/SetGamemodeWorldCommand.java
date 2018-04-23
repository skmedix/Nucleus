/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.ImprovedGameModeArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.NucleusWorldPropertiesArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.storage.WorldProperties;

@NonnullByDefault
@NoModifiers
@Permissions(prefix = "world", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = {"setgamemode", "setgm", "gamemode", "gm"}, subcommandOf = WorldCommand.class)
public class SetGamemodeWorldCommand extends AbstractCommand<CommandSource> {

    private final String gamemode = "gamemode";
    private final String world = "world";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.onlyOne(new ImprovedGameModeArgument(Text.of(this.gamemode))),
            GenericArguments.optional(GenericArguments.onlyOne(new NucleusWorldPropertiesArgument(Text.of(this.world), NucleusWorldPropertiesArgument.Type.ALL)))
        };
    }

    @Override
    public CommandResult executeCommand(final CommandSource src, CommandContext args) throws Exception {
        GameMode gamemodeInput = args.<GameMode>getOne(this.gamemode).get();
        WorldProperties worldProperties = getWorldFromUserOrArgs(src, this.world, args);

        worldProperties.setGameMode(gamemodeInput);
        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.setgamemode.success",
            worldProperties.getWorldName(),
            Util.getTranslatableIfPresent(gamemodeInput)));

        return CommandResult.success();
    }
}
