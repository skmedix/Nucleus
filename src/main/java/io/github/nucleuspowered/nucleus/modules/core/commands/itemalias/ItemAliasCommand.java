/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.commands.itemalias;

import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.modules.core.commands.NucleusCommand;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NoModifiers
@Permissions(prefix = "nucleus")
@RegisterCommand(value = {"itemalias", "item"}, subcommandOf = NucleusCommand.class, hasExecutor = false)
@NonnullByDefault
public class ItemAliasCommand extends AbstractCommand<CommandSource> {

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) {
        return CommandResult.empty();
    }
}
