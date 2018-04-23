/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands;

import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * Allows a user to warp to the specified warp.
 *
 * Command Usage: /world Permission: plugin.world.base
 *
 */
@Permissions(suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = "world", rootAliasRegister = { "nworld", "nucleusworld" }, hasExecutor = false)
@NonnullByDefault
public class WorldCommand extends AbstractCommand<CommandSource> {

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) {
        return CommandResult.empty();
    }
}
