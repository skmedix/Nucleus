/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.commands.itemalias;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.configurate.datatypes.ItemDataNode;
import io.github.nucleuspowered.nucleus.dataservices.ItemDataService;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.function.Function;

@RunAsync
@NoModifiers
@Permissions(prefix = "nucleus.itemalias")
@RegisterCommand(value = {"remove", "del"}, subcommandOf = ItemAliasCommand.class)
@NonnullByDefault
public class RemoveItemAliasCommand extends AbstractCommand<CommandSource> {

    private final ItemDataService itemDataService = Nucleus.getNucleus().getItemDataService();
    private final String alias = "alias";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.onlyOne(
                    GenericArguments.choices(Text.of(this.alias), this.itemDataService::getAliases, Function.identity()))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) {
        String al = args.<String>getOne(this.alias).get();
        String id = this.itemDataService.getIdFromAlias(al).get();
        ItemDataNode node = this.itemDataService.getDataForItem(id);
        node.removeAlias(al);
        this.itemDataService.setDataForItem(id, node);

        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.nucleus.removeitemalias.removed", al, id));
        return CommandResult.success();
    }
}
