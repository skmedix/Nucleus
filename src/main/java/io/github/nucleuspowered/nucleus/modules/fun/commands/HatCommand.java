/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.fun.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Optional;

@RegisterCommand({"hat", "head"})
@NoModifiers
@Permissions(supportsSelectors = true, supportsOthers = true)
@EssentialsEquivalent({"hat", "head"})
@NonnullByDefault
public class HatCommand extends AbstractCommand.SimpleTargetOtherPlayer {

    @Override protected CommandResult executeWithPlayer(CommandSource player, Player pl, CommandContext args, boolean isSelf) throws Exception {
        Optional<ItemStack> helmetOptional = pl.getHelmet();

        ItemStack stack = pl.getItemInHand(HandTypes.MAIN_HAND)
                .orElseThrow(() -> ReturnMessageException.fromKey("command.generalerror.handempty"));
        ItemStack hand = stack.copy();
        hand.setQuantity(1);
        pl.setHelmet(hand);
        Text itemName = hand.get(Keys.DISPLAY_NAME).orElseGet(() -> Text.of(stack));

        GameMode gameMode = pl.get(Keys.GAME_MODE).orElse(GameModes.NOT_SET);
        if (gameMode != GameModes.CREATIVE) {
            if (stack.getQuantity() > 1) {
                stack.setQuantity(stack.getQuantity() - 1);
                pl.setItemInHand(HandTypes.MAIN_HAND, stack);
            } else {
                pl.setItemInHand(HandTypes.MAIN_HAND, null);
            }
        }

        // If the old item can't be placed back in the subject inventory, drop the item.
        helmetOptional.ifPresent(itemStack -> Util.getStandardInventory(pl).offer(itemStack.copy())
                .getRejectedItems().forEach(x -> Util.dropItemOnFloorAtLocation(x, pl.getWorld(), pl.getLocation().getPosition())));

        if (!isSelf) {
            player.sendMessage(Nucleus.getNucleus()
                    .getMessageProvider().getTextMessageWithTextFormat("command.hat.success", Nucleus.getNucleus().getNameUtil().getName(pl), itemName));
        }

        pl.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithTextFormat("command.hat.successself", itemName));
        return CommandResult.success();
    }
}