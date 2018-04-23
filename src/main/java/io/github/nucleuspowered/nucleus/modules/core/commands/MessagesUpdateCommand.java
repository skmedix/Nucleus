/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.messages.ConfigMessageProvider;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;

@RunAsync
@NoModifiers
@Permissions(prefix = "nucleus", suggestedLevel = SuggestedLevel.OWNER)
@RegisterCommand(value = "update-messages", subcommandOf = NucleusCommand.class)
@NonnullByDefault
public class MessagesUpdateCommand extends AbstractCommand<CommandSource> {

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.flags().flag("y").buildWith(GenericArguments.none())
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        // First, reload the messages.
        boolean reload = Nucleus.getNucleus().reloadMessages();
        if (!reload) { // only false if we can't read the custom messages file.
            // There was a failure loading a custom file
            throw ReturnMessageException.fromKey("command.nucleus.messageupdate.couldnotload");
        }

        MessageProvider messageProvider = Nucleus.getNucleus().getMessageProvider();
        if (!(messageProvider instanceof ConfigMessageProvider)) {
            throw new ReturnMessageException(messageProvider.getTextMessageWithFormat("command.nucleus.messageupdate.notfile"));
        }

        ConfigMessageProvider cmp = (ConfigMessageProvider)messageProvider;
        List<String> keys = cmp.checkForMigration();
        src.sendMessage(messageProvider.getTextMessageWithFormat("command.nucleus.messageupdate.reloaded"));
        if (keys.isEmpty()) {
            return CommandResult.success();
        }

        if (args.hasAny("y")) {
            cmp.reset(keys);
            src.sendMessage(messageProvider.getTextMessageWithFormat("command.nucleus.messageupdate.reset"));
        } else {
            src.sendMessage(messageProvider.getTextMessageWithFormat("command.nucleus.messageupdate.sometoupdate", String.valueOf(keys.size())));
            keys.forEach(x -> src.sendMessage(Text.of(TextColors.YELLOW, x)));
            src.sendMessage(messageProvider.getTextMessageWithFormat("command.nucleus.messageupdate.confirm", "/nucleus update-messages -y").toBuilder()
                .onClick(TextActions.runCommand("/nucleus update-messages -y")).build());
        }

        return CommandResult.success();
    }
}
