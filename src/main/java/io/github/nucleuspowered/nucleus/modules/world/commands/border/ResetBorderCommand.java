/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands.border;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.Optional;

@Permissions(prefix = "world.border", mainOverride = "set")
@NoModifiers
@RegisterCommand(value = {"reset"}, subcommandOf = BorderCommand.class)
@NonnullByDefault
public class ResetBorderCommand extends AbstractCommand<CommandSource> {

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                NucleusParameters.OPTIONAL_WORLD_PROPERTIES_ALL,
        };
    }

    @Override protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        WorldProperties wp = getWorldFromUserOrArgs(src, NucleusParameters.Keys.WORLD, args);
        wp.setWorldBorderCenter(0, 0);
        Optional<World> world = Sponge.getServer().getWorld(wp.getUniqueId());

        // A world to get defaults from.
        World toDiameter = world.orElseGet(() -> Sponge.getServer().getWorld(
            Sponge.getServer().getDefaultWorld().orElseThrow(IllegalStateException::new).getUniqueId()).orElseThrow(IllegalStateException::new));

        // +1 includes the final block (1 -> -1 would otherwise give 2, not 3).
        final long diameter = Math.abs(toDiameter.getBiomeMax().getX() - toDiameter.getBiomeMin().getX()) + 1;
        wp.setWorldBorderDiameter(diameter);

        world.ifPresent(w -> {
            w.getWorldBorder().setCenter(0, 0);
            w.getWorldBorder().setDiameter(diameter);
        });

        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.setborder.set",
                wp.getWorldName(),
                "0",
                "0",
                String.valueOf(diameter)));

        return CommandResult.success();
    }
}
