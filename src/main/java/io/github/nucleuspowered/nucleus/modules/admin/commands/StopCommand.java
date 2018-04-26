/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.admin.commands;

import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoTimings;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Optional;

@Permissions
@NoModifiers
@NoTimings
@RegisterCommand({"stop"})
@NonnullByDefault
public class StopCommand extends AbstractCommand<CommandSource> {

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                NucleusParameters.OPTIONAL_MESSAGE
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) {
        Optional<String> opt = args.getOne(NucleusParameters.Keys.MESSAGE);
        if (opt.isPresent()) {
            Sponge.getServer().shutdown(TextSerializers.FORMATTING_CODE.deserialize(opt.get()));
        } else {
            Sponge.getServer().shutdown();
        }

        return CommandResult.success();
    }
}
