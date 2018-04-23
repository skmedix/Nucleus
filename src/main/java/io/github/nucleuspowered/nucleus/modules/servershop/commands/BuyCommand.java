/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.servershop.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.ItemAliasArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.PositiveIntegerArgument;
import io.github.nucleuspowered.nucleus.configurate.datatypes.ItemDataNode;
import io.github.nucleuspowered.nucleus.dataservices.ItemDataService;
import io.github.nucleuspowered.nucleus.internal.EconHelper;
import io.github.nucleuspowered.nucleus.internal.annotations.RequiresEconomy;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.servershop.config.ServerShopConfigAdapter;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Collection;
import java.util.function.Consumer;

@RunAsync
@NoModifiers
@RequiresEconomy
@Permissions(suggestedLevel = SuggestedLevel.USER)
@RegisterCommand({"itembuy", "buy"})
@EssentialsEquivalent("buy")
@NonnullByDefault
public class BuyCommand extends AbstractCommand<Player> implements Reloadable {

    private final ItemDataService itemDataService = Nucleus.getNucleus().getItemDataService();
    private final EconHelper econHelper = Nucleus.getNucleus().getEconHelper();
    private final String itemKey = "item";
    private final String amountKey = "amount";
    private int max;

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.flags().flag("y", "f", "a", "-yes", "-auto").buildWith(
                GenericArguments.seq(
                    GenericArguments.onlyOne(new ItemAliasArgument(Text.of(this.itemKey))),
                    GenericArguments.onlyOne(new PositiveIntegerArgument(Text.of(this.amountKey)))))
        };
    }

    @Override
    public CommandResult executeCommand(final Player src, CommandContext args) {
        CatalogType ct = args.<CatalogType>getOne(this.itemKey).get();
        int amount = args.<Integer>getOne(this.amountKey).get();

        ItemDataNode node = this.itemDataService.getDataForItem(ct.getId());
        if (node.getServerBuyPrice() < 0) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.itembuy.notforsale"));
            return CommandResult.empty();
        }

        if (amount > this.max) {
            amount = this.max;
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.itembuy.maximum", String.valueOf(amount)));
        }

        final ItemStack created;
        if (ct instanceof ItemType) {
            created = ItemStack.of((ItemType) ct, amount);
        } else {
            created = ItemStack.builder().fromBlockState((BlockState)ct).quantity(amount).build();
        }

        // Get the cost.
        final double perUnitCost = node.getServerBuyPrice();
        final int unitCount = amount;
        final double overallCost = perUnitCost * unitCount;
        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithTextFormat("command.itembuy.summary",
                Text.of(String.valueOf(amount)), Text.of(created),
                Text.of(this.econHelper.getCurrencySymbol(overallCost))));

        if (args.hasAny("y")) {
            new BuyCallback(src, overallCost, created, unitCount, perUnitCost).accept(src);
        } else {
            src.sendMessage(
                    Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.itembuy.clickhere").toBuilder()
                            .onClick(TextActions.executeCallback(new BuyCallback(src, overallCost, created, unitCount, perUnitCost))).build()
            );
        }

        return CommandResult.success();
    }

    @Override public void onReload() {
        this.max = getServiceUnchecked(ServerShopConfigAdapter.class).getNodeOrDefault().getMaxPurchasableAtOnce();
    }

    private class BuyCallback implements Consumer<CommandSource> {

        private final Player src;
        private final double overallCost;
        private final ItemStack created;
        private final int unitCount;
        private final double perUnitCost;

        private boolean hasRun = false;

        private BuyCallback(Player src, double overallCost, ItemStack created, int unitCount, double perUnitCost) {
            this.src = src;
            this.overallCost = overallCost;
            this.created = created;
            this.unitCount = unitCount;
            this.perUnitCost = perUnitCost;
        }

        @Override
        public void accept(CommandSource source) {
            if (this.hasRun) {
                return;
            }

            this.hasRun = true;

            // Get the money, transact, return the money on fail.
            Inventory target = Util.getStandardInventory(this.src);
            if (BuyCommand.this.econHelper.withdrawFromPlayer(this.src, this.overallCost, false)) {
                Text name = Text.of(this.created);
                InventoryTransactionResult itr = target.offer(this.created);
                if (itr.getRejectedItems().isEmpty()) {
                    this.src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithTextFormat("command.itembuy.transactionsuccess",
                            Text.of(this.unitCount), name, Text.of(BuyCommand.this.econHelper.getCurrencySymbol(this.overallCost))));
                } else {
                    Collection<ItemStackSnapshot> iss = itr.getRejectedItems();
                    int rejected = iss.stream().mapToInt(ItemStackSnapshot::getQuantity).sum();
                    double refund = rejected * this.perUnitCost;
                    BuyCommand.this.econHelper.depositInPlayer(this.src, refund, false);
                    this.src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithTextFormat("command.itembuy.transactionpartial",
                            Text.of(rejected), name));
                    this.src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithTextFormat("command.itembuy.transactionsuccess",
                            Text.of(String.valueOf(this.unitCount - rejected)), name,
                            Text.of(BuyCommand.this.econHelper.getCurrencySymbol(this.overallCost - refund))));
                }
            } else {
                // No funds.
                this.src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.itembuy.nofunds"));
            }
        }
    }
}
