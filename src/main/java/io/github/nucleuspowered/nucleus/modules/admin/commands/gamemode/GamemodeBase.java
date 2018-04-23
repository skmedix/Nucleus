/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.admin.commands.gamemode;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;

import java.util.HashMap;
import java.util.Map;

abstract class GamemodeBase<T extends CommandSource> extends AbstractCommand<T> {

    private static final Map<String, String> MODE_MAP = new HashMap<String, String>() {{
        put(GameModes.SURVIVAL.getId(), "modes.survival");
        put(GameModes.CREATIVE.getId(), "modes.creative");
        put(GameModes.ADVENTURE.getId(), "modes.adventure");
        put(GameModes.SPECTATOR.getId(), "modes.spectator");
    }};

    CommandResult baseCommand(CommandSource src, Player user, GameMode gm) throws Exception {

        if (!this.permissions.testSuffix(src, MODE_MAP.computeIfAbsent(
                gm.getId(), key -> {
                    String[] keySplit = key.split(":", 2);
                    String r = keySplit[keySplit.length - 1].toLowerCase();
                    MODE_MAP.put(key, "modes." + r);
                    return "modes." + r;
                }
        ))) {
            throw new ReturnMessageException(
                    Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.gamemode.permission", gm.getTranslation().get()));
        }

        DataTransactionResult dtr = user.offer(Keys.GAME_MODE, gm);
        if (dtr.isSuccessful()) {
            if (!src.equals(user)) {
                src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.gamemode.set.other", user.getName(), gm.getName()));
            }

            user.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.gamemode.set.base", gm.getName()));
            return CommandResult.success();
        }

        throw ReturnMessageException.fromKey("command.gamemode.error", user.getName());
    }
}
