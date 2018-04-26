/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands.kit;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import io.github.nucleuspowered.nucleus.internal.annotations.Since;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.kit.commands.KitFallbackBase;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Optional;

@Permissions(prefix = "kit", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = {"edit", "ed"}, subcommandOf = KitCommand.class)
@NoModifiers
@NonnullByDefault
@Since(spongeApiVersion = "5.0", minecraftVersion = "1.10.2", nucleusVersion = "0.13")
public class KitEditCommand extends KitFallbackBase<Player> {

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
                KitFallbackBase.KIT_PARAMETER_NO_PERM_CHECK
        };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        final Kit kitInfo = args.<Kit>getOne(KIT_PARAMETER_KEY).get();

        if (KIT_HANDLER.isOpen(kitInfo.getName())) {
            throw new ReturnMessageException(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.kit.edit.current", kitInfo.getName()));
        }

        Inventory inventory = Util.getKitInventoryBuilder()
            .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Nucleus.getNucleus().getMessageProvider()
                    .getTextMessageWithFormat("command.kit.edit.title", kitInfo.getName())))
            .build(Nucleus.getNucleus());

        kitInfo.getStacks().stream().filter(x -> !x.getType().equals(ItemTypes.NONE)).forEach(x -> inventory.offer(x.createStack()));
        Optional<Container> openedInventory = src.openInventory(inventory);

        if (openedInventory.isPresent()) {
            KIT_HANDLER.addKitInventoryToListener(Tuple.of(kitInfo, inventory), openedInventory.get());
            return CommandResult.success();
        }

        throw ReturnMessageException.fromKey("command.kit.edit.cantopen", kitInfo.getName());
    }
}
