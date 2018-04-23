/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.commands.itemalias;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.argumentparsers.ItemAliasArgument;
import io.github.nucleuspowered.nucleus.configurate.datatypes.ItemDataNode;
import io.github.nucleuspowered.nucleus.dataservices.ItemDataService;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@RunAsync
@NoModifiers
@Permissions(prefix = "nucleus.itemalias")
@RegisterCommand(value = {"clear"}, subcommandOf = ItemAliasCommand.class)
@NonnullByDefault
public class ClearItemAliasesCommand extends AbstractCommand<CommandSource> {

    private final ItemDataService itemDataService = Nucleus.getNucleus().getItemDataService();
    private final String item = "item";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.onlyOne(new ItemAliasArgument(Text.of(this.item)))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) {
        CatalogType al = args.<CatalogType>getOne(this.item).get();
        String id = al.getId().toLowerCase();
        ItemDataNode node = this.itemDataService.getDataForItem(id);
        node.clearAliases();
        this.itemDataService.setDataForItem(id, node);

        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.nucleus.removeitemalias.cleared", id));
        return CommandResult.success();
    }
}
