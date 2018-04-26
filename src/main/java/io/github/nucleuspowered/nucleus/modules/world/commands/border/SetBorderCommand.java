/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands.border;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.argumentparsers.PositiveIntegerArgument;
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
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.Optional;

@Permissions(prefix = "world.border")
@NoModifiers
@RegisterCommand(value = {"set"}, subcommandOf = BorderCommand.class)
@NonnullByDefault
public class SetBorderCommand extends AbstractCommand<CommandSource> {

    private final String xKey = "x";
    private final String zKey = "z";
    private final String diameter = "diameter";
    private final String delayKey = "delay";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.firstParsing(
                // Console + player
                GenericArguments.seq(
                    NucleusParameters.OPTIONAL_WORLD_PROPERTIES_ALL,
                    GenericArguments.onlyOne(GenericArguments.integer(Text.of(this.xKey))),
                    GenericArguments.onlyOne(GenericArguments.integer(Text.of(this.zKey))),
                    GenericArguments.onlyOne(new PositiveIntegerArgument(Text.of(this.diameter))),
                    GenericArguments.onlyOne(GenericArguments.optional(GenericArguments.onlyOne(new PositiveIntegerArgument(Text.of(this.delayKey)))))
                ),

                // Player only
                GenericArguments.seq(
                    GenericArguments.onlyOne(new PositiveIntegerArgument(Text.of(this.diameter))),
                    GenericArguments.onlyOne(GenericArguments.optional(GenericArguments.onlyOne(new PositiveIntegerArgument(Text.of(this.delayKey))))))
            )
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        WorldProperties wp = getWorldFromUserOrArgs(src, NucleusParameters.Keys.WORLD, args);
        int x;
        int z;
        int dia = args.<Integer>getOne(this.diameter).get();
        int delay = args.<Integer>getOne(this.delayKey).orElse(0);

        if (src instanceof Locatable) {
            Location<World> lw = ((Locatable) src).getLocation();
            if (args.hasAny(this.zKey)) {
                x = args.<Integer>getOne(this.xKey).get();
                z = args.<Integer>getOne(this.zKey).get();
            } else {
                x = lw.getBlockX();
                z = lw.getBlockZ();
            }
        } else {
            x = args.<Integer>getOne(this.xKey).get();
            z = args.<Integer>getOne(this.zKey).get();
        }

        // Now, if we have an x and a z key, get the centre from that.
        wp.setWorldBorderCenter(x, z);
        Optional<World> world = Sponge.getServer().getWorld(wp.getUniqueId());
        world.ifPresent(w -> w.getWorldBorder().setCenter(x, z));

        wp.setWorldBorderCenter(x, z);

        if (delay == 0) {
            world.ifPresent(w -> w.getWorldBorder().setDiameter(dia));
            wp.setWorldBorderDiameter(dia);
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.setborder.set",
                    wp.getWorldName(),
                    String.valueOf(x),
                    String.valueOf(z),
                    String.valueOf(dia)));
        } else {
            world.ifPresent(w -> w.getWorldBorder().setDiameter(dia, delay * 1000L));
            wp.setWorldBorderTimeRemaining(delay * 1000L);
            wp.setWorldBorderTargetDiameter(dia);
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.setborder.setdelay",
                    wp.getWorldName(),
                    String.valueOf(x),
                    String.valueOf(z),
                    String.valueOf(dia),
                    String.valueOf(delay)));
        }


        return CommandResult.success();
    }


}
