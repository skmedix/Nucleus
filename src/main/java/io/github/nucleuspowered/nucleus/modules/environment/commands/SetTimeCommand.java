/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.environment.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.WorldTimeArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.storage.WorldProperties;

@Permissions(prefix = "time")
@RegisterCommand(value = "set", subcommandOf = TimeCommand.class, rootAliasRegister = { "settime", "timeset" })
@EssentialsEquivalent(value = {"time", "day", "night"}, isExact = false, notes = "A time MUST be specified.")
public class SetTimeCommand extends AbstractCommand<CommandSource> {
    private final String time = "time";
    private final String world = "world";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.optionalWeak(GenericArguments.onlyOne(GenericArguments.world(Text.of(world)))),
            GenericArguments.onlyOne(new WorldTimeArgument(Text.of(time)))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        WorldProperties pr = getWorldPropertiesOrDefault(src, world, args);

        int tick = args.<Integer>getOne(time).get();
        pr.setWorldTime(tick);
        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.settime.done", String.valueOf(Util.getTimeFromTicks(tick))));
        return CommandResult.success();
    }
}
