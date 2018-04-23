/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.item.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@Permissions
@RegisterCommand({"more", "stack"})
@EssentialsEquivalent("more")
@NonnullByDefault
public class MoreCommand extends AbstractCommand<Player> {

    @Override
    public CommandResult executeCommand(Player player, CommandContext args) {

        if (player.getItemInHand(HandTypes.MAIN_HAND).isPresent()) {
            ItemStack stack = player.getItemInHand(HandTypes.MAIN_HAND).get();
            stack.setQuantity(stack.getMaxStackQuantity());
            player.setItemInHand(HandTypes.MAIN_HAND, stack);
            player.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.more.success", stack.getType().getName(),
                    String.valueOf(stack.getType().getMaxStackQuantity())));
            return CommandResult.success();
        }

        player.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.more.none"));
        return CommandResult.empty();
    }
}
