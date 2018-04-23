/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.misc.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.data.manipulator.mutable.entity.FoodData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@Permissions(supportsOthers = true)
@RegisterCommand({"feed", "eat"})
@EssentialsEquivalent({"feed", "eat"})
@NonnullByDefault
public class FeedCommand extends AbstractCommand.SimpleTargetOtherPlayer {

    @Override
    protected CommandResult executeWithPlayer(CommandSource src, Player pl, CommandContext args, boolean isSelf) {
        // Get the food data and modify it.
        FoodData foodData = pl.getFoodData();
        Value<Integer> f = foodData.foodLevel().set(foodData.foodLevel().getDefault());
        Value<Double> d = foodData.saturation().set(foodData.saturation().getDefault());
        foodData.set(f, d);

        if (pl.offer(foodData).isSuccessful()) {
            pl.sendMessages(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.feed.success.self"));
            if (!pl.equals(src)) {
                src.sendMessages(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.feed.success.other", pl.getName()));
            }

            return CommandResult.success();
        } else {
            src.sendMessages(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.feed.error"));
            return CommandResult.empty();
        }
    }
}
