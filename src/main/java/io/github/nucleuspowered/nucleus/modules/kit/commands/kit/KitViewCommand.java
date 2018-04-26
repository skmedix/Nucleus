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
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.kit.commands.KitFallbackBase;
import io.github.nucleuspowered.nucleus.modules.kit.config.KitConfigAdapter;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.stream.Collectors;

@Permissions(prefix = "kit", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = {"view"}, subcommandOf = KitCommand.class)
@NoModifiers
@NonnullByDefault
@Since(spongeApiVersion = "7.0", minecraftVersion = "1.12.1", nucleusVersion = "1.2")
public class KitViewCommand extends KitFallbackBase<Player> implements Reloadable {

    private boolean processTokens = false;

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
                KitFallbackBase.KIT_PARAMETER_PERM_CHECK
        };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        final Kit kitInfo = args.<Kit>getOne(KIT_PARAMETER_KEY).get();

        Inventory inventory = Util.getKitInventoryBuilder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Nucleus.getNucleus().getMessageProvider()
                        .getTextMessageWithFormat("command.kit.view.title", kitInfo.getName())))
                .build(Nucleus.getNucleus());

        List<ItemStack> lis = kitInfo.getStacks().stream().filter(x -> !x.getType().equals(ItemTypes.NONE)).map(ItemStackSnapshot::createStack)
                .collect(Collectors.toList());
        if (this.processTokens) {
            KIT_HANDLER.processTokensInItemStacks(src, lis);
        }

        lis.forEach(inventory::offer);
        return src.openInventory(inventory)
            .map(x -> {
                KIT_HANDLER.addViewer(x);
                return CommandResult.success();
            })
            .orElseThrow(() -> ReturnMessageException.fromKey("command.kit.view.cantopen", kitInfo.getName()));
    }

    @Override
    public void onReload() {
        this.processTokens = getServiceUnchecked(KitConfigAdapter.class).getNodeOrDefault().isProcessTokens();
    }
}
