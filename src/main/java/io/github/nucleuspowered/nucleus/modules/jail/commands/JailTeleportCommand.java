/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jail.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.nucleusdata.NamedLocation;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.jail.JailParameters;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;

@NoModifiers
@NonnullByDefault
@RegisterCommand(value = "tp", subcommandOf = JailsCommand.class)
@Permissions(prefix = "jail", mainOverride = "list", suggestedLevel = SuggestedLevel.MOD)
public class JailTeleportCommand extends AbstractCommand<Player> {

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                JailParameters.JAIL
        };
    }

    @Override protected CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        NamedLocation location = args.<NamedLocation>getOne(JailParameters.JAIL_KEY).get();
        Transform<World> location1 = location.getTransform().orElseThrow(
                () -> new ReturnMessageException(
                        Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.jails.tp.noworld", location.getName()))
        );

        src.setTransform(location1);
        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.jails.tp.success", location.getName()));
        return CommandResult.success();
    }
}
