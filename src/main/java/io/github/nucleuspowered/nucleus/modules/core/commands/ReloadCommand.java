/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@Permissions(prefix = "nucleus")
@NoModifiers
@RegisterCommand(value = "reload", subcommandOf = NucleusCommand.class)
@NonnullByDefault
public class ReloadCommand extends AbstractCommand<CommandSource> {

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        if (Nucleus.getNucleus().reload()) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.reload.one"));
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.reload.two"));
            return CommandResult.success();
        }

        throw new ReturnMessageException(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.reload.errorone"));
    }
}
