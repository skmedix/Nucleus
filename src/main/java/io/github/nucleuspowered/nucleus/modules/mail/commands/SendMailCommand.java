/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mail.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.mail.handlers.MailHandler;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@Permissions(prefix = "mail", suggestedLevel = SuggestedLevel.USER)
@RunAsync
@RegisterCommand(value = {"send", "s"}, subcommandOf = MailCommand.class, rootAliasRegister = "sendmail")
@NonnullByDefault
public class SendMailCommand extends AbstractCommand<CommandSource> {

    private final MailHandler handler = getServiceUnchecked(MailHandler.class);
    private final String perm = getPermissionHandlerFor(MailCommand.class).getBase();

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                NucleusParameters.ONE_USER,
                NucleusParameters.MESSAGE
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        User pl = args.<User>getOne(NucleusParameters.Keys.USER).orElseThrow(() -> new CommandException(
                Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.user.none")));

        // Only send mails to players that can read them.
        if (!hasPermission(pl, this.perm)) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.mail.send.error", pl.getName()));
            return CommandResult.empty();
        }

        // Send the message.
        String m = args.<String>getOne(NucleusParameters.Keys.MESSAGE).orElseThrow(() -> new CommandException(
                Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.message.none")));
        if (src instanceof User) {
            this.handler.sendMail((User) src, pl, m);
        } else {
            this.handler.sendMailFromConsole(pl, m);
        }

        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.mail.send.successful", pl.getName()));
        return CommandResult.success();
    }
}
