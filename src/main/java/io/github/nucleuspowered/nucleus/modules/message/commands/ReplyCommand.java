/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.message.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoHelpSubcommand;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NotifyIfAFK;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RedirectModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.message.handlers.MessageHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * Replies to the last player who sent a message.
 */
@Permissions(mainOverride = "message", suggestedLevel = SuggestedLevel.USER)
@NoHelpSubcommand
@RedirectModifiers(value = "message")
@RegisterCommand({"reply", "r"})
@EssentialsEquivalent({"r", "reply"})
@NonnullByDefault
@NotifyIfAFK(NucleusParameters.Keys.PLAYER)
public class ReplyCommand extends AbstractCommand<CommandSource> {

    private final MessageHandler handler = getServiceUnchecked(MessageHandler.class);

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                NucleusParameters.MESSAGE
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) {
        boolean b = this.handler.replyMessage(src, args.<String>getOne(NucleusParameters.Keys.MESSAGE).get());
        if (b) {
            // For Notify on AFK
            this.handler.getLastMessageFrom(Util.getUUID(src)).ifPresent(x -> args.putArg(NucleusParameters.Keys.PLAYER, x));
            return CommandResult.success();
        }

        return CommandResult.empty();
    }

}
