/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands.border;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularWorldService;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.modules.world.WorldHelper;
import io.github.nucleuspowered.nucleus.modules.world.datamodules.WorldgenWorldDataModule;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.storage.WorldProperties;

@NoModifiers
@NonnullByDefault
@Permissions(prefix = "world.border", mainOverride = "gen")
@RegisterCommand(value = "cancelgen", subcommandOf = BorderCommand.class)
public class CancelChunkGenCommand extends AbstractCommand<CommandSource> {

    private final WorldHelper worldHelper = getServiceUnchecked(WorldHelper.class);

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                NucleusParameters.OPTIONAL_WORLD_PROPERTIES_ENABLED_ONLY
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        WorldProperties wp = getWorldFromUserOrArgs(src, NucleusParameters.Keys.WORLD, args);
        ModularWorldService m = Nucleus.getNucleus().getWorldDataManager().getWorld(wp.getUniqueId()).get();
        m.set(m.get(WorldgenWorldDataModule.class).setStart(false));
        m.save();
        if (this.worldHelper.cancelPregenRunningForWorld(wp.getUniqueId())) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.cancelgen.cancelled", wp.getWorldName()));
            return CommandResult.success();
        }

        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.cancelgen.notask", wp.getWorldName()));
        return CommandResult.empty();
    }
}
