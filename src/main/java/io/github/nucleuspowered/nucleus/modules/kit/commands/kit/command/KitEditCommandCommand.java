/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands.kit.command;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import io.github.nucleuspowered.nucleus.argumentparsers.KitArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.kit.handlers.KitHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.inject.Inject;

@NoModifiers
@NonnullByDefault
@RunAsync
@Permissions(prefix = "kit.command", suggestedLevel = SuggestedLevel.NONE)
@RegisterCommand(value = {"edit"}, subcommandOf = KitCommandCommand.class)
public class KitEditCommandCommand extends AbstractCommand<Player> {

    private final String key = "kit";
    private final KitHandler handler;

    @Inject
    public KitEditCommandCommand(KitHandler handler) {
        this.handler = handler;
    }

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
            new KitArgument(Text.of(key), false)
        };
    }

    @Override protected CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        final Kit kitInfo = args.<Kit>getOne(key).get();
        List<String> commands = kitInfo.getCommands();
        if (commands.size() > 9 * 6) {
            throw ReturnMessageException.fromKey("command.kit.command.edit.toomany", kitInfo.getName());
        }

        // Create an inventory with signed books.
        Random r = new Random();
        List<ItemStack> books = commands.stream()
                .map(x -> {
                    ItemStack stack = ItemStack.of(ItemTypes.WRITTEN_BOOK, 1);
                    Text command = Text.of(x);
                    stack.offer(Keys.DISPLAY_NAME, command);
                    stack.offer(Keys.BOOK_PAGES, Lists.newArrayList(command));
                    stack.offer(Keys.BOOK_AUTHOR, Text.of(kitInfo.getName(), "-", r.nextInt())); // So books don't stack.
                    return stack;
                }).collect(Collectors.toList());

        // Create Inventory GUI.
        final InventoryTitle title = InventoryTitle.of(Text.of("Kit Commands: ", kitInfo.getName()));
        final Inventory inventory = Inventory.builder().of(InventoryArchetypes.DOUBLE_CHEST)
            .property(InventoryTitle.PROPERTY_NAME, title).build(plugin);
        books.forEach(inventory::offer);

        src.openInventory(inventory, Cause.of(NamedCause.owner(plugin), NamedCause.source(src)))
            .ifPresent(x -> handler.addKitCommandInventoryToListener(Tuple.of(kitInfo, inventory), x));

        return CommandResult.success();
    }
}
