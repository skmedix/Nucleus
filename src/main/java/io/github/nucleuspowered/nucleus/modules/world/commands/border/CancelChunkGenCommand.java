/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands.border;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.argumentparsers.NucleusWorldPropertiesArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.modules.world.WorldHelper;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.storage.WorldProperties;

@NoModifiers
@NonnullByDefault
@Permissions(prefix = "world.border", mainOverride = "gen")
@RegisterCommand(value = "cancelgen", subcommandOf = BorderCommand.class)
public class CancelChunkGenCommand extends AbstractCommand<CommandSource> {

    private final String worldKey = "world";

    private final WorldHelper worldHelper = getServiceUnchecked(WorldHelper.class);

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.optional(GenericArguments.onlyOne(new NucleusWorldPropertiesArgument(Text.of(this.worldKey), NucleusWorldPropertiesArgument.Type.ENABLED_ONLY)))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        WorldProperties wp = getWorldFromUserOrArgs(src, this.worldKey, args);
        if (this.worldHelper.cancelPregenRunningForWorld(wp.getUniqueId())) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.cancelgen.cancelled", wp.getWorldName()));
            return CommandResult.success();
        }

        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.cancelgen.notask", wp.getWorldName()));
        return CommandResult.empty();
    }
}
