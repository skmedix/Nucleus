/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.argumentparsers.NucleusWorldPropertiesArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.storage.WorldProperties;

@NoModifiers
@Permissions(prefix = "world", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = { "disable", "dis" }, subcommandOf = WorldCommand.class)
@NonnullByDefault
public class DisableWorldCommand extends AbstractCommand<CommandSource> {

    private final String worldKey = "world";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.onlyOne(new NucleusWorldPropertiesArgument(Text.of(this.worldKey), NucleusWorldPropertiesArgument.Type.ENABLED_ONLY))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        WorldProperties worldProperties = args.<WorldProperties>getOne(this.worldKey).get();
        if (!worldProperties.isEnabled()) {
            throw new ReturnMessageException(
                    Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.disable.alreadydisabled", worldProperties.getWorldName()));
        }

        if (Sponge.getServer().getWorld(worldProperties.getUniqueId()).isPresent()) {
            throw new ReturnMessageException(
                    Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.disable.warnloaded", worldProperties.getWorldName()));
        }

        worldProperties.setEnabled(false);
        if (worldProperties.isEnabled()) {
            throw new ReturnMessageException(
                    Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.disable.couldnotdisable", worldProperties.getWorldName()));
        }

        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.disable.success", worldProperties.getWorldName()));
        return CommandResult.success();
    }
}
