/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jail.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.LocationData;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.modules.jail.JailParameters;
import io.github.nucleuspowered.nucleus.modules.jail.handlers.JailHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@Permissions(prefix = "jail")
@RunAsync
@NoModifiers
@RegisterCommand(value = {"delete", "del", "remove"}, subcommandOf = JailsCommand.class, rootAliasRegister = { "deljail", "rmjail", "deletejail" })
@EssentialsEquivalent({"deljail", "remjail", "rmjail"})
@NonnullByDefault
public class DeleteJailCommand extends AbstractCommand<CommandSource> {

    private final JailHandler handler = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(JailHandler.class);

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                JailParameters.JAIL
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) {
        LocationData wl = args.<LocationData>getOne(JailParameters.JAIL_KEY).get();
        if (this.handler.removeJail(wl.getName())) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.jails.del.success", wl.getName()));
            return CommandResult.success();
        }

        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.jails.del.error", wl.getName()));
        return CommandResult.empty();
    }
}
