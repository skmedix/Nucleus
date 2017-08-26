/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands.kit;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import io.github.nucleuspowered.nucleus.argumentparsers.KitArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.Since;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.kit.config.KitConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.kit.handlers.KitHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

@Permissions(prefix = "kit", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = {"view"}, subcommandOf = KitCommand.class)
@NoModifiers
@NonnullByDefault
@Since(spongeApiVersion = "7.0", minecraftVersion = "1.12.1", nucleusVersion = "1.2")
public class KitViewCommand extends AbstractCommand<Player> implements AbstractCommand.Reloadable {

    private final KitConfigAdapter kitConfigAdapter;
    private final KitHandler kitHandler;
    private final String kitKey = "kit";
    private boolean processTokens = false;

    @Inject
    public KitViewCommand(KitHandler kitHandler, KitConfigAdapter kca) {
        this.kitHandler = kitHandler;
        this.kitConfigAdapter = kca;
    }

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.onlyOne(new KitArgument(Text.of(this.kitKey), true))
        };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        final Kit kitInfo = args.<Kit>getOne(this.kitKey).get();

        Inventory inventory = Util.getKitInventoryBuilder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(plugin.getMessageProvider()
                        .getTextMessageWithFormat("command.kit.view.title", kitInfo.getName())))
                .build(this.plugin);

        List<ItemStack> lis = kitInfo.getStacks().stream().filter(x -> !x.getType().equals(ItemTypes.NONE)).map(ItemStackSnapshot::createStack)
                .collect(Collectors.toList());
        if (this.processTokens) {
            this.kitHandler.processTokensInItemStacks(src, lis);
        }

        lis.forEach(inventory::offer);
        return src.openInventory(inventory, Cause.of(NamedCause.owner(this.plugin), NamedCause.source(src)))
            .map(x -> {
                kitHandler.addViewer(x);
                return CommandResult.success();
            })
            .orElseThrow(() -> ReturnMessageException.fromKey("command.kit.view.cantopen", kitInfo.getName()));
    }

    @Override
    public void onReload() {
        this.processTokens = this.kitConfigAdapter.getNodeOrDefault().isProcessTokens();
    }
}
