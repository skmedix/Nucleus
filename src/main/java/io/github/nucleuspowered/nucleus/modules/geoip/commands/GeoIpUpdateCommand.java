/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.geoip.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.modules.geoip.handlers.GeoIpDatabaseHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@RunAsync
@NoModifiers
@NonnullByDefault
@Permissions(prefix = "geoip")
@RegisterCommand(value = "update", subcommandOf = GeoIpCommand.class)
public class GeoIpUpdateCommand extends AbstractCommand<CommandSource> {

    @Override public CommandResult executeCommand(CommandSource src, CommandContext args) {
        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.geoip.update.start"));
        try {
            Nucleus.getNucleus().getInternalServiceManager().getService(GeoIpDatabaseHandler.class).get().load(
                GeoIpDatabaseHandler.LoadType.DOWNLOAD);
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.geoip.update.complete"));
        } catch (IllegalStateException e) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.geoip.update.licence"));
        } catch (Exception e) {
            if (Nucleus.getNucleus().isDebugMode()) {
                e.printStackTrace();
            }
        }

        return CommandResult.success();
    }
}
