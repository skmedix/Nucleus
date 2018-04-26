/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.freezeplayer.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.modules.freezeplayer.datamodules.FreezePlayerUserDataModule;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@Permissions(supportsOthers = true)
@RegisterCommand({"freezeplayer", "freeze"})
@NonnullByDefault
public class FreezePlayerCommand extends AbstractCommand<CommandSource> {

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.optionalWeak(GenericArguments.requiringPermission(
                        NucleusParameters.ONE_PLAYER, this.permissions.getPermissionWithSuffix("others"))),
                GenericArguments.optional(NucleusParameters.ONE_TRUE_FALSE)
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        User pl = this.getUserFromArgs(User.class, src, NucleusParameters.Keys.PLAYER, args);
        FreezePlayerUserDataModule nu = Nucleus.getNucleus().getUserDataManager().getUnchecked(pl).get(FreezePlayerUserDataModule.class);
        nu.setFrozen(args.<Boolean>getOne(NucleusParameters.Keys.BOOL).orElseGet(() -> !nu.isFrozen()));
        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat(
            nu.isFrozen() ? "command.freezeplayer.success.frozen" : "command.freezeplayer.success.unfrozen",
                Nucleus.getNucleus().getNameUtil().getSerialisedName(pl)));
        return CommandResult.success();
    }
}
