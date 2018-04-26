/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.admin.commands.exp;

import io.github.nucleuspowered.nucleus.argumentparsers.ExperienceLevelArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.PositiveIntegerArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.modules.admin.commands.ExperienceCommand;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NoModifiers
@Permissions(prefix = "exp")
@RegisterCommand(value = "give", subcommandOf = ExperienceCommand.class)
@NonnullByDefault
public class GiveExperience extends AbstractCommand<CommandSource> {

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                NucleusParameters.OPTIONAL_ONE_PLAYER,
                GenericArguments.firstParsing(
                    GenericArguments.onlyOne(new ExperienceLevelArgument(Text.of(ExperienceCommand.levelKey))),
                    GenericArguments.onlyOne(new PositiveIntegerArgument(Text.of(ExperienceCommand.experienceKey)))
                )
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Player pl = this.getUserFromArgs(Player.class, src, NucleusParameters.Keys.PLAYER, args);
        if (!ExperienceCommand.checkGameMode(pl, src)) {
            return CommandResult.empty();
        }

        int extra;
        if (args.hasAny(ExperienceCommand.levelKey)) {
            int lvl = pl.get(Keys.EXPERIENCE_LEVEL).orElse(0) + args.<Integer>getOne(ExperienceCommand.levelKey).get();
            extra = pl.get(Keys.EXPERIENCE_SINCE_LEVEL).orElse(0);

            // Offer level, then we offer the extra experience.
            pl.tryOffer(Keys.EXPERIENCE_LEVEL, lvl);
        } else {
            extra = args.<Integer>getOne(ExperienceCommand.experienceKey).get();
        }

        int exp = pl.get(Keys.TOTAL_EXPERIENCE).get();
        exp += extra;

        return ExperienceCommand.tellUserAboutExperience(src, pl, pl.offer(Keys.TOTAL_EXPERIENCE, exp).isSuccessful());
    }
}
