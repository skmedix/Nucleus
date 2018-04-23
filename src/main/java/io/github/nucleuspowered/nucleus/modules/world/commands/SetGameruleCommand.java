/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
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
import org.spongepowered.api.world.storage.WorldProperties;

@NoModifiers
@NonnullByDefault
@Permissions(prefix = "world.gamerule", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = { "set" }, subcommandOf = GameruleCommand.class)
public class SetGameruleCommand extends AbstractCommand<CommandSource> {

    private static final String worldKey = "world";
    private static final String gameRuleKey = "gamerule";
    private static final String valueKey = "value";

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.optionalWeak(GenericArguments.onlyOne(GenericArguments.world(Text.of(worldKey)))),
            GenericArguments.string(Text.of(gameRuleKey)),
            GenericArguments.string(Text.of(valueKey))
        };
    }

    @Override public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        WorldProperties worldProperties = getWorldFromUserOrArgs(src, worldKey, args);
        String gameRule = args.<String>getOne(gameRuleKey).get();
        String value = args.<String>getOne(valueKey).get();

        worldProperties.setGameRule(gameRule, value);

        src.sendMessage(
                Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.gamerule.set.success", gameRule, value, worldProperties.getWorldName()));
        return CommandResult.success();
    }
}
