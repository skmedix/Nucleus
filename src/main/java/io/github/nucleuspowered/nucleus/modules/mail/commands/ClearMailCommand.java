/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mail.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.mail.handlers.MailHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@Permissions(mainOverride = "mail", suggestedLevel = SuggestedLevel.USER)
@NoModifiers
@RunAsync
@RegisterCommand(value = "clear", subcommandOf = MailCommand.class)
@NonnullByDefault
public class ClearMailCommand extends AbstractCommand<Player> {

    private final MailHandler handler = getServiceUnchecked(MailHandler.class);

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) {
        if (this.handler.clearUserMail(src)) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.mail.clear.success"));
        } else {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.mail.clear.nomail"));
        }

        return CommandResult.success();
    }
}
