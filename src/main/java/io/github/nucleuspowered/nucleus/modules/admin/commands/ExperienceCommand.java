/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.admin.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.entity.ExperienceHolderData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@RegisterCommand({"exp", "experience", "xp"})
@Permissions
@NoModifiers
@EssentialsEquivalent({"exp", "xp"})
@NonnullByDefault
public class ExperienceCommand extends AbstractCommand<CommandSource> {

    public static final String experienceKey = "experience";
    public static final String levelKey = "level";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                NucleusParameters.OPTIONAL_ONE_PLAYER
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws ReturnMessageException {
        Player pl = getUserFromArgs(Player.class, src, NucleusParameters.Keys.PLAYER, args);

        ExperienceHolderData ehd = pl.get(ExperienceHolderData.class).get();
        int exp = ehd.totalExperience().get();
        int lv = ehd.level().get();

        src.sendMessage(
                Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.exp.info", pl.getName(), String.valueOf(exp), String.valueOf(lv)));
        return CommandResult.success();
    }

    public static CommandResult tellUserAboutExperience(CommandSource src, Player pl, boolean isSuccess) {
        if (!isSuccess) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.exp.set.error"));
            return CommandResult.empty();
        }

        int exp = pl.get(Keys.TOTAL_EXPERIENCE).get();
        int newLvl = pl.get(Keys.EXPERIENCE_LEVEL).get();

        if (!src.equals(pl)) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.exp.set.new.other", pl.getName(), String.valueOf(exp), String.valueOf(newLvl)));
        }

        pl.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.exp.set.new.self", String.valueOf(exp), String.valueOf(newLvl)));
        return CommandResult.success();
    }

    public static boolean checkGameMode(Player pl, CommandSource src) {
        GameMode gm = pl.get(Keys.GAME_MODE).orElse(GameModes.SURVIVAL);
        if (gm == GameModes.CREATIVE || gm == GameModes.SPECTATOR) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.exp.gamemode", pl.getName()));
            return false;
        }

        return true;
    }
}
