/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.ImprovedCatalogTypeArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.NucleusWorldPropertiesArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.CatalogTypes;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.storage.WorldProperties;

@NoModifiers
@NonnullByDefault
@Permissions(prefix = "world", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = {"setdifficulty", "difficulty"}, subcommandOf = WorldCommand.class)
public class SetDifficultyWorldCommand extends AbstractCommand<CommandSource> {

    private final String difficulty = "difficulty";
    private final String world = "world";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.onlyOne(new ImprovedCatalogTypeArgument(Text.of(difficulty), CatalogTypes.DIFFICULTY)),
                GenericArguments.optional(GenericArguments.onlyOne(new NucleusWorldPropertiesArgument(Text.of(world), NucleusWorldPropertiesArgument.Type.ALL)))
        };
    }

    @Override
    public CommandResult executeCommand(final CommandSource src, CommandContext args) throws Exception {
        Difficulty difficultyInput = args.<Difficulty>getOne(difficulty).get();
        WorldProperties worldProperties = getWorldFromUserOrArgs(src, this.world, args);

        worldProperties.setDifficulty(difficultyInput);
        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.world.setdifficulty.success",
                worldProperties.getWorldName(),
                Util.getTranslatableIfPresent(difficultyInput)));

        return CommandResult.success();
    }
}
