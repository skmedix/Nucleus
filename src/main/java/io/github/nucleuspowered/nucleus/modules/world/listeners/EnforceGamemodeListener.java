/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.listeners;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.world.WorldModule;
import io.github.nucleuspowered.nucleus.modules.world.config.WorldConfig;
import io.github.nucleuspowered.nucleus.modules.world.config.WorldConfigAdapter;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.world.World;

import java.util.Map;
import java.util.Set;

public class EnforceGamemodeListener implements ListenerBase.Conditional {

    private final String perm = "nucleus.world.force-gamemode.override";

    @Override
    public Map<String, PermissionInformation> getPermissions() {
        return ImmutableMap.of(
                this.perm,
                PermissionInformation.getWithTranslation("permission.world.force-gamemode.override", SuggestedLevel.ADMIN)
        );
    }

    @Listener(order = Order.POST)
    public void onPlayerLogin(ClientConnectionEvent.Join event, @Getter("getTargetEntity") Player player) {
        Task.builder().execute(() -> enforce(player, player.getWorld())).submit(Nucleus.getNucleus());
    }

    @Listener(order = Order.POST)
    public void onPlayerTeleport(MoveEntityEvent.Teleport event,
            @Getter("getTargetEntity") Player player,
            @Getter("getFromTransform") Transform<World> from,
            @Getter("getToTransform") Transform<World> to) {
        if (!from.getExtent().getUniqueId().equals(to.getExtent().getUniqueId())) {
            enforce(player, to.getExtent());
        }
    }

    private void enforce(Player player, World world) {
        if (world.getProperties().getGameMode() == GameModes.NOT_SET) {
            return;
        }

        Set<Context> contextSet = Sets.newHashSet(player.getActiveContexts());
        contextSet.removeIf(x -> x.getKey().equals(Context.WORLD_KEY));
        contextSet.add(new Context(Context.WORLD_KEY, world.getName()));
        if (!player.hasPermission(contextSet, this.perm)) {
            // set their gamemode accordingly.
            player.offer(Keys.GAME_MODE, world.getProperties().getGameMode());
        }
    }

    @Override
    public boolean shouldEnable() {
        return Nucleus.getNucleus().getConfigValue(WorldModule.ID, WorldConfigAdapter.class, WorldConfig::isEnforceGamemodeOnWorldChange).orElse(false);
    }

}
