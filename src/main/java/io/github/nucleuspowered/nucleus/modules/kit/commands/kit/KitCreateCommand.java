/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands.kit;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.Since;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.kit.commands.KitFallbackBase;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@Permissions(prefix = "kit", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = {"create"}, subcommandOf = KitCommand.class)
@NoModifiers
@NonnullByDefault
@Since(spongeApiVersion = "5.0", minecraftVersion = "1.10.2", nucleusVersion = "0.13")
public class KitCreateCommand extends KitFallbackBase<CommandSource> {

    private final String name = "name";

    @Override
    protected boolean allowFallback(CommandSource source, CommandArgs args, CommandContext context) {
        if (context.hasAny(this.name)) {
            return false;
        }
        return super.allowFallback(source, args, context);
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.onlyOne(GenericArguments.string(Text.of(this.name)))
        };
    }

    @Override
    public CommandResult executeCommand(final CommandSource source, CommandContext args) throws ReturnMessageException {
        String kitName = args.<String>getOne(this.name).get();

        if (KIT_HANDLER.getKitNames().stream().anyMatch(kitName::equalsIgnoreCase)) {
            throw new ReturnMessageException(Nucleus
                    .getNucleus().getMessageProvider().getTextMessageWithFormat("command.kit.add.alreadyexists", kitName));
        }

        if (source instanceof Player) {
            final Player player = (Player)source;
            Inventory inventory = Util.getKitInventoryBuilder()
                    .property(InventoryTitle.PROPERTY_NAME,
                            InventoryTitle.of(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.kit.create.title", kitName)))
                    .build(Nucleus.getNucleus());
            Container container = player.openInventory(inventory)
                    .orElseThrow(() -> ReturnMessageException.fromKey("command.kit.create.notcreated"));
            Sponge.getEventManager().registerListeners(Nucleus.getNucleus(), new TemporaryEventListener(inventory, container, kitName));
        } else {
            try {
                KIT_HANDLER.saveKit(KIT_HANDLER.createKit(kitName));
                source.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.kit.addempty.success", kitName));
            } catch (IllegalArgumentException ex) {
                throw ReturnMessageException.fromKey("command.kit.create.failed", kitName);
            }
        }

        return CommandResult.success();
    }

    public class TemporaryEventListener {

        private final Inventory inventory;
        private final Container container;
        private final String kitName;
        private boolean run = false;

        private TemporaryEventListener(Inventory inventory, Container container, String kitName) {
            this.inventory = inventory;
            this.container = container;
            this.kitName = kitName;
        }

        @Listener
        public void onClose(InteractInventoryEvent.Close event, @Root Player player, @Getter("getTargetInventory") Container container) {
            if (!this.run && this.container.equals(container)) {
                this.run = true;
                Sponge.getEventManager().unregisterListeners(this);

                if (KIT_HANDLER.getKitNames().stream().noneMatch(this.kitName::equalsIgnoreCase)) {
                    KIT_HANDLER.saveKit(KIT_HANDLER.createKit(this.kitName).updateKitInventory(this.inventory));
                    player.sendMessage(
                            Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.kit.add.success", this.kitName));
                } else {
                    player.sendMessage(
                            Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.kit.add.alreadyexists", this.kitName));
                }

                // Now return the items to the subject.
                this.inventory.slots().forEach(x -> x.poll().ifPresent(item -> player.getInventory().offer(item)));
            }
        }
    }
}
