/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.powertool.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.CommandPermissionHandler;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.powertool.commands.PowertoolCommand;
import io.github.nucleuspowered.nucleus.modules.powertool.datamodules.PowertoolUserDataModule;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.filter.type.Exclude;
import org.spongepowered.api.item.ItemType;

public class PowertoolListener implements ListenerBase {

    private final CommandPermissionHandler permissionRegistry =
            Nucleus.getNucleus().getPermissionRegistry().getPermissionsForNucleusCommand(PowertoolCommand.class);

    @Listener
    @Exclude(InteractBlockEvent.class)
    public void onUserInteract(final InteractEvent event, @Root Player player) {
        // No item in hand or no permission -> no powertool.
        if (!this.permissionRegistry.testBase(player) || !player.getItemInHand(HandTypes.MAIN_HAND).isPresent()) {
            return;
        }

        // Get the item and the user.
        ItemType item = player.getItemInHand(HandTypes.MAIN_HAND).get().getType();
        PowertoolUserDataModule user;
        try {
            user = Nucleus.getNucleus().getUserDataManager().getUnchecked(player).get(PowertoolUserDataModule.class);
        } catch (Exception e) {
            Nucleus.getNucleus().printStackTraceIfDebugMode(e);
            return;
        }

        // If the powertools are toggled on.
        if (user.isPowertoolToggled()) {
            // Execute all powertools if they exist.
            user.getPowertoolForItem(item).ifPresent(x -> {
                // Cancel the interaction.
                event.setCancelled(true);

                final Player interacting;
                if (event instanceof InteractEntityEvent && ((InteractEntityEvent) event).getTargetEntity() instanceof Player) {
                    interacting = (Player)((InteractEntityEvent) event).getTargetEntity();
                } else {
                    interacting = null;
                }

                // Run each command.
                if (interacting == null && x.stream().allMatch(i -> i.contains("{{subject}}"))) {
                    player.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("powertool.playeronly"));
                    return;
                }

                x.forEach(s -> {
                    if (s.contains("{{subject}}")) {
                        if (interacting != null) {
                            s = s.replace("{{subject}}", interacting.getName());
                        } else {
                            // Don't execute when no subject is in the way.
                            return;
                        }
                    }

                    Sponge.getCommandManager().process(player, s);
                });
            });
        }
    }
}
