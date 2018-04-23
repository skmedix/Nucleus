/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warp.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Warp;
import io.github.nucleuspowered.nucleus.api.service.NucleusWarpService;
import io.github.nucleuspowered.nucleus.argumentparsers.WarpArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.modules.warp.event.DeleteWarpEvent;
import io.github.nucleuspowered.nucleus.util.CauseStackHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * Deletes a warp.
 *
 * Command Usage: /warp delete [warp] Permission: quickstart.warp.delete.base
 */
@Permissions(prefix = "warp")
@RunAsync
@RegisterCommand(value = {"delete", "del"}, subcommandOf = WarpCommand.class)
@EssentialsEquivalent({"delwarp", "remwarp", "rmwarp"})
@NonnullByDefault
public class DeleteWarpCommand extends AbstractCommand<CommandSource> {

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.onlyOne(new WarpArgument(Text.of(WarpCommand.warpNameArg), false))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Warp warp = args.<Warp>getOne(WarpCommand.warpNameArg).get();
        NucleusWarpService qs = Sponge.getServiceManager().provideUnchecked(NucleusWarpService.class);

        DeleteWarpEvent event = new DeleteWarpEvent(CauseStackHelper.createCause(src), warp);
        if (Sponge.getEventManager().post(event)) {
            throw new ReturnMessageException(event.getCancelMessage().orElseGet(() ->
                    Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("nucleus.eventcancelled")
            ));
        }

        if (qs.removeWarp(warp.getName())) {
            // Worked. Tell them.
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.warps.del", warp.getName()));
            return CommandResult.success();
        }

        // Didn't work. Tell them.
        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.warps.delerror"));
        return CommandResult.empty();
    }

}
