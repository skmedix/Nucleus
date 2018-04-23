/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.servershop.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.configurate.datatypes.ItemDataNode;
import io.github.nucleuspowered.nucleus.dataservices.ItemDataService;
import io.github.nucleuspowered.nucleus.internal.EconHelper;
import io.github.nucleuspowered.nucleus.internal.annotations.RequiresEconomy;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Optional;

@RunAsync
@NoModifiers
@RequiresEconomy
@Permissions(suggestedLevel = SuggestedLevel.USER)
@RegisterCommand({"itemsell", "sell"})
@EssentialsEquivalent("sell")
@NonnullByDefault
public class SellCommand extends AbstractCommand<Player> {

    private final ItemDataService itemDataService = Nucleus.getNucleus().getItemDataService();
    private final EconHelper econHelper = Nucleus.getNucleus().getEconHelper();

    @Override
    public CommandResult executeCommand(final Player src, CommandContext args) throws Exception {
        // Get the item in the hand.
        ItemStack is = src.getItemInHand(HandTypes.MAIN_HAND).orElseThrow(() -> new ReturnMessageException(
                Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.generalerror.handempty")));
        String id;
        Optional<BlockState> blockState = is.get(Keys.ITEM_BLOCKSTATE);
        id = blockState.map(blockState1 -> blockState1.getId().toLowerCase()).orElseGet(() -> is.getType().getId());

        ItemDataNode node = this.itemDataService.getDataForItem(id);
        final double sellPrice = node.getServerSellPrice();
        if (sellPrice < 0) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.itemsell.notforselling"));
            return CommandResult.empty();
        }

        // Get the cost.
        final int amt = is.getQuantity();
        final double overallCost = sellPrice * amt;
        if (this.econHelper.depositInPlayer(src, overallCost, false)) {
            src.setItemInHand(HandTypes.MAIN_HAND, null);
            src.sendMessage(Nucleus.getNucleus().getMessageProvider()
                    .getTextMessageWithTextFormat("command.itemsell.summary", Text.of(amt), Text.of(is),
                            Text.of(this.econHelper.getCurrencySymbol(overallCost))));
            return CommandResult.success();
        }

        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithTextFormat("command.itemsell.error", Text.of(is)));
        return CommandResult.empty();
    }
}
