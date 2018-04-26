/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mail.commands;

import io.github.nucleuspowered.nucleus.argumentparsers.MailFilterArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.modules.mail.handlers.MailHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@Permissions(prefix = "mail")
@RunAsync
@NoModifiers
@RegisterCommand(value = {"other", "o"}, subcommandOf = MailCommand.class)
@NonnullByDefault
public class MailOtherCommand extends AbstractCommand<CommandSource> {

    private final MailHandler handler = getServiceUnchecked(MailHandler.class);

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                NucleusParameters.ONE_USER,
                GenericArguments.optional(GenericArguments.allOf(new MailFilterArgument(Text.of(MailReadBase.filters), this.handler)))};
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) {
        return MailReadBase.INSTANCE.executeCommand(src, args.<User>getOne(NucleusParameters.Keys.USER).get(), args.getAll(MailReadBase.filters));
    }
}
