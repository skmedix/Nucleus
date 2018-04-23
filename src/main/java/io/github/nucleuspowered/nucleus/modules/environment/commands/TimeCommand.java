/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.environment.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.storage.WorldProperties;

@Permissions(suggestedLevel = SuggestedLevel.USER)
@RegisterCommand("time")
@NonnullByDefault
@EssentialsEquivalent(value = {"time"}, isExact = false, notes = "This just displays the time. Use '/time set' to set the time.")
public class TimeCommand extends AbstractCommand<CommandSource> {

    private final String world = "world";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] { GenericArguments.optionalWeak(GenericArguments.onlyOne(GenericArguments.world(Text.of(this.world)))) };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) {
        WorldProperties pr = getWorldPropertiesOrDefault(src, this.world, args);

        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.time", pr.getWorldName(), String.valueOf(Util.getTimeFromTicks(pr.getWorldTime()))));
        return CommandResult.success();
    }
}
