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
@RegisterCommand(value = "set", subcommandOf = ItemAliasCommand.class)
@NonnullByDefault
public class SetItemAliasCommand extends AbstractCommand<CommandSource> {

    private final ItemDataService itemDataService = Nucleus.getNucleus().getItemDataService();

    private final String item = "item";
    private final String alias = "alias";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.optionalWeak(new ItemAliasArgument(Text.of(this.item))),
            GenericArguments.string(Text.of(this.alias))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        // Do we have an item or blockstate?
        String a = args.<String>getOne(this.alias).get().toLowerCase();
        if (this.itemDataService.getIdFromAlias(a).isPresent()) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.nucleus.setitemalias.inuse", a));
            return CommandResult.empty();
        }

        if (!ItemDataNode.ALIAS_PATTERN.matcher(a).matches()) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.nucleus.setitemalias.notvalid", a));
            return CommandResult.empty();
        }

        CatalogType type = getCatalogTypeFromHandOrArgs(src, this.item, args);

        // Set the alias.
        String id = type.getId().toLowerCase();
        ItemDataNode idn = this.itemDataService.getDataForItem(id);
        idn.addAlias(a);
        this.itemDataService.setDataForItem(id, idn);

        // Tell the user
        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.nucleus.setitemalias.success", a, id));
        return CommandResult.success();
    }
}
