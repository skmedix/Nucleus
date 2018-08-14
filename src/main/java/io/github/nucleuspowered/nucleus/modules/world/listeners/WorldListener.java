/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.listeners;

import com.google.common.collect.Sets;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.modules.world.WorldModule;
import io.github.nucleuspowered.nucleus.modules.world.config.WorldConfig;
import io.github.nucleuspowered.nucleus.modules.world.config.WorldConfigAdapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.teleport.TeleportHelperFilters;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class WorldListener implements ListenerBase.Conditional {

    private final Set<UUID> messageSent = Sets.newHashSet();

    @Listener
    public void onPlayerTeleport(MoveEntityEvent.Teleport event, @Getter("getTargetEntity") Player player) {
        World target = event.getToTransform().getExtent();
        if (player.getWorld().equals(target)) return;

        if (!hasPermission(player, PermissionRegistry.PERMISSIONS_PREFIX + "worlds." + target.getName().toLowerCase())) {
            event.setCancelled(true);
            if (!this.messageSent.contains(player.getUniqueId())) {
                player.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("world.access.denied", target.getName()));
            }

            if (event instanceof MoveEntityEvent.Teleport.Portal) {
                this.messageSent.add(player.getUniqueId());
                Sponge.getScheduler().createTaskBuilder()
                    .delayTicks(1)
                    .execute(relocate(player))
                    .submit(Nucleus.getNucleus());
            }
        }
    }

    @Override
    public boolean shouldEnable() {
        return Nucleus.getNucleus().getConfigValue(WorldModule.ID, WorldConfigAdapter.class, WorldConfig::isSeparatePermissions).orElse(false);
    }

    private Consumer<Task> relocate(Player player) {
        return task -> {
            Optional<Location<World>> location = Sponge.getTeleportHelper().getSafeLocationWithBlacklist(player.getLocation(), 5, 5, 5, TeleportHelperFilters.NO_PORTAL);
            if (location.isPresent()) {
                player.setLocation(location.get());
            } else {
                player.setLocationSafely(player.getWorld().getSpawnLocation());
            }

            this.messageSent.remove(player.getUniqueId());
        };
    }

}
