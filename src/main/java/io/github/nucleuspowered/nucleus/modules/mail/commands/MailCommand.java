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
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.mail.handlers.MailHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@Permissions(suggestedLevel = SuggestedLevel.USER)
@RunAsync
@NoModifiers
@RegisterCommand({"mail", "email"})
@EssentialsEquivalent({"mail", "email"})
@NonnullByDefault
public class MailCommand extends AbstractCommand<Player> {

    private final MailHandler handler = getServiceUnchecked(MailHandler.class);

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] { GenericArguments.optional(GenericArguments.allOf(new MailFilterArgument(Text.of(MailReadBase.filters),
                this.handler))) };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) {
        return MailReadBase.INSTANCE.executeCommand(src, src, args.getAll(MailReadBase.filters));
    }
}
