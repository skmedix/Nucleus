/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.List;

@Permissions(prefix = "world", mainOverride = "list")
@NonnullByDefault
@RegisterCommand(value = "info", subcommandOf = WorldCommand.class)
public class InfoWorldCommand extends AbstractCommand<CommandSource> {

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
                NucleusParameters.WORLD_PROPERTIES_ALL
        };
    }

    @Override protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        WorldProperties wp = getWorldFromUserOrArgs(src, NucleusParameters.Keys.WORLD, args);
        final List<Text> listContent = Lists.newArrayList();
        final boolean canSeeSeeds = this.permissions.testSuffix(src, "seed");
        ListWorldCommand.getWorldInfo(listContent, wp, canSeeSeeds);
        Util.getPaginationBuilder(src)
            .contents(listContent).title(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.info.title", wp.getWorldName()))
            .sendTo(src);

        return CommandResult.success();
    }
}
