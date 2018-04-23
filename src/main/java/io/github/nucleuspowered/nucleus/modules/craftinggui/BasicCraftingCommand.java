/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.craftinggui;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
public abstract class BasicCraftingCommand extends AbstractCommand<Player> {

    protected abstract InventoryArchetype getArchetype();

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        Inventory i = Inventory.builder().of(getArchetype())
            .build(Nucleus.getNucleus());
        src.openInventory(i)
                .orElseThrow(() -> new ReturnMessageException(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.crafting.error")));
        return CommandResult.success();
    }
}
