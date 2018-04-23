/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.admin.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.internal.text.NucleusTextTemplateFactory;
import io.github.nucleuspowered.nucleus.internal.text.NucleusTextTemplateMessageSender;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
@RunAsync
@NoModifiers
@Permissions(suggestedLevel = SuggestedLevel.OWNER)
@RegisterCommand({ "plainbroadcast", "pbcast", "pbc" })
public class PlainBroadcastCommand extends AbstractCommand<CommandSource> {
    private final String message = "message";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] { GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(Text.of(this.message))) };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        try {
            new NucleusTextTemplateMessageSender(NucleusTextTemplateFactory.createFromString(args.<String>getOne(this.message).get()), src).send();
        } catch (Throwable throwable) {
            if (Nucleus.getNucleus().isDebugMode()) {
                throwable.printStackTrace();
            }

            throw ReturnMessageException.fromKey("command.plainbroadcast.failed");
        }
        return CommandResult.success();
    }
}
