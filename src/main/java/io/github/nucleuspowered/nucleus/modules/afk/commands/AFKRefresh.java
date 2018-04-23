/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.modules.afk.handlers.AFKHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@Permissions
@RegisterCommand("afkrefresh")
@NonnullByDefault
public class AFKRefresh extends AbstractCommand<CommandSource> {

    private final AFKHandler handler = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(AFKHandler.class);

    @Override public CommandResult executeCommand(CommandSource src, CommandContext args) {
        this.handler.invalidateAfkCache();
        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.afkrefresh.complete"));
        return CommandResult.success();
    }
}
