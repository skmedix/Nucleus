/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.admin.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
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
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Collection;

@Permissions(supportsSelectors = true)
@RegisterCommand("kill")
@EssentialsEquivalent(value = { "kill", "remove", "butcher", "killall", "mobkill" },
        isExact = false, notes = "Nucleus supports killing entities using the Minecraft selectors.")
@NonnullByDefault
public class KillCommand extends AbstractCommand<CommandSource> {

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                NucleusParameters.MANY_ENTITY
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Collection<Entity> entities = args.getAll(NucleusParameters.Keys.SUBJECT);

        int entityKillCount = 0;
        int playerKillCount = 0;
        for (Entity x : entities) {
            if (x instanceof Player) {
                Player pl = (Player)x;
                GameMode gm = pl.gameMode().getDirect().orElseGet(() -> pl.gameMode().getDefault());
                if (gm != GameModes.SURVIVAL && gm != GameModes.NOT_SET) {
                    if (entities.size() == 1) {
                        throw ReturnMessageException.fromKey("command.kill.wronggm", pl.getName());
                    } else {
                        continue;
                    }
                }
            }

            DataTransactionResult dtr = x.offer(Keys.HEALTH, 0d);
            if (!dtr.isSuccessful() && !(x instanceof Living)) {
                x.remove();
            }
            entityKillCount++;

            if (x instanceof Player) {
                playerKillCount++;
                src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.kill.killed",
                        Nucleus.getNucleus().getNameUtil().getSerialisedName((Player)x)));
                ((Player)x).sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.kill.killedby", src.getName()));
            }
        }

        if (entityKillCount > playerKillCount) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.kill.overall", String.valueOf(entityKillCount),
                    String.valueOf(playerKillCount)));
        }

        return CommandResult.success();
    }
}
