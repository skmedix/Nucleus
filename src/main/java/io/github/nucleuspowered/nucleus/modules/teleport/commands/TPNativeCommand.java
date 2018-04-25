/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.commands;

import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoPermissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * This is a wrapper class for /minecraft:tp to map to "tpn", for those who want
 * that.
 */
// TODO: Remove and remap /minecraft:tp
@NonnullByDefault
@NoModifiers
@NoPermissions
@RegisterCommand({"teleportnative", "tpnative", "tpn"})
public class TPNativeCommand extends AbstractCommand<CommandSource> {

    private final String a = "args";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.remainingJoinedStrings(Text.of(this.a))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) {
        Sponge.getCommandManager().process(src, String.format("minecraft:tp %s", args.<String>getOne(this.a).orElse("")));
        return CommandResult.success();
    }
}
