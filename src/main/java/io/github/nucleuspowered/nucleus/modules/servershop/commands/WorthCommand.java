/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.servershop.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.ItemAliasArgument;
import io.github.nucleuspowered.nucleus.configurate.datatypes.ItemDataNode;
import io.github.nucleuspowered.nucleus.dataservices.ItemDataService;
import io.github.nucleuspowered.nucleus.internal.EconHelper;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@RunAsync
@RegisterCommand("worth")
@Permissions(suggestedLevel = SuggestedLevel.USER)
@EssentialsEquivalent({"worth", "price"})
@NonnullByDefault
public class WorthCommand extends AbstractCommand<CommandSource> {

    private final String item = "item";
    private final ItemDataService itemDataService = Nucleus.getNucleus().getItemDataService();
    private final EconHelper econHelper = Nucleus.getNucleus().getEconHelper();

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.optionalWeak(new ItemAliasArgument(Text.of(this.item)))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        CatalogType type = getCatalogTypeFromHandOrArgs(src, this.item, args);
        String id = type.getId();

        // Get the item from the system.
        ItemDataNode node = this.itemDataService.getDataForItem(id);

        // Get the current item worth.
        MessageProvider provider = Nucleus.getNucleus().getMessageProvider();
        if (!this.econHelper.economyServiceExists()) {
            src.sendMessage(provider.getTextMessageWithFormat("command.setworth.noeconservice"));
        }

        double buyPrice = node.getServerBuyPrice();
        double sellPrice = node.getServerSellPrice();

        StringBuilder stringBuilder = new StringBuilder();

        if (buyPrice >= 0) {
            stringBuilder.append(provider.getMessageWithFormat("command.worth.buy", this.econHelper.getCurrencySymbol(node.getServerBuyPrice())));
        }

        if (sellPrice >= 0) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(" - ");
            }

            stringBuilder.append(provider.getMessageWithFormat("command.worth.sell", this.econHelper.getCurrencySymbol(node.getServerSellPrice())));
        }

        if (stringBuilder.length() == 0) {
            src.sendMessage(provider.getTextMessageWithFormat("command.worth.nothing", Util.getTranslatableIfPresentOnCatalogType(type)));
        } else {
            src.sendMessage(provider.getTextMessageWithFormat("command.worth.something", Util.getTranslatableIfPresentOnCatalogType(type), stringBuilder.toString()));
        }

        return CommandResult.success();
    }
}
