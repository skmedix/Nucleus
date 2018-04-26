/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.Optional;

@NoModifiers
@NonnullByDefault
@Permissions(prefix = "world", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = {"load"}, subcommandOf = WorldCommand.class)
public class LoadWorldCommand extends AbstractCommand<CommandSource> {

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.flags().permissionFlag(
                    Nucleus.getNucleus().getPermissionRegistry().getPermissionsForNucleusCommand(EnableWorldCommand.class).getBase(), "e", "-enable")
                .buildWith(NucleusParameters.WORLD_PROPERTIES_ENABLED_ONLY)
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        WorldProperties worldProperties = args.<WorldProperties>getOne(NucleusParameters.Keys.WORLD).get();
        if (!worldProperties.isEnabled() && !args.hasAny("e")) {
            // Not enabled, cannot load.
            if (Nucleus.getNucleus().getPermissionRegistry().getPermissionsForNucleusCommand(EnableWorldCommand.class).testBase(src)) {
                throw new ReturnMessageException(
                        Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.load.notenabled.enable", worldProperties.getWorldName()));
            }

            throw new ReturnMessageException(
                    Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.load.notenabled.noenable", worldProperties.getWorldName()));
        }

        if (Sponge.getServer().getWorld(worldProperties.getUniqueId()).isPresent()) {
            throw new ReturnMessageException(
                    Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.load.alreadyloaded", worldProperties.getWorldName()));
        }

        worldProperties.setEnabled(true);
        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.load.start", worldProperties.getWorldName()));
        Optional<World> optional = Sponge.getServer().loadWorld(worldProperties);
        if (optional.isPresent()) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.load.loaded", worldProperties.getWorldName()));
            return CommandResult.success();
        }

        throw new ReturnMessageException(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.load.fail",
                worldProperties.getWorldName()));
    }


}
