/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands;

import com.flowpowered.math.vector.Vector3i;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.storage.WorldProperties;

@Permissions(prefix = "world", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = {"setspawn"}, subcommandOf = WorldCommand.class)
@NonnullByDefault
public class SetSpawnWorldCommand extends AbstractCommand<CommandSource> {

    private final String worldKey = "world";
    private final String xKey = "x";
    private final String yKey = "y";
    private final String zKey = "z";

    @Override
    protected CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.optional(
                        GenericArguments.seq(
                                GenericArguments.world(Text.of(this.worldKey)),
                                GenericArguments.integer(Text.of(this.xKey)),
                                GenericArguments.integer(Text.of(this.yKey)),
                                GenericArguments.integer(Text.of(this.zKey))
                        )
                )
        };
    }

    @Override
    protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        WorldProperties world = this.getWorldFromUserOrArgs(src, this.worldKey, args);
        Vector3i loc;
        if (args.hasAny(this.xKey)) {
            loc = new Vector3i(
                    args.<Integer>getOne(this.xKey).get(),
                    args.<Integer>getOne(this.yKey).get(),
                    args.<Integer>getOne(this.zKey).get()
            );
        } else {
            loc = ((Locatable) src).getLocation().getBlockPosition();
        }

        world.setSpawnPosition(loc);
        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.setspawn.success"));
        return CommandResult.success();
    }
}
