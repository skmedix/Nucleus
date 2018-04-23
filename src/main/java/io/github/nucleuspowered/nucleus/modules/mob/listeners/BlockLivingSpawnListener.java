/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mob.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.mob.MobModule;
import io.github.nucleuspowered.nucleus.modules.mob.config.BlockSpawnsConfig;
import io.github.nucleuspowered.nucleus.modules.mob.config.MobConfig;
import io.github.nucleuspowered.nucleus.modules.mob.config.MobConfigAdapter;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;

public class BlockLivingSpawnListener implements Reloadable, ListenerBase.Conditional {

    private MobConfig config = new MobConfig();

    @Listener
    public void onConstruct(ConstructEntityEvent.Pre event, @Getter("getTransform") Transform<World> worldTransform, @Getter("getTargetType") EntityType type) {
        // No, let's not prevent players from spawning...
        Class<? extends Entity> entityType = type.getEntityClass();
        if (!checkIsValid(entityType) && !isSpawnable(type, worldTransform.getExtent())) {
            event.setCancelled(true);
        }
    }

    // Most will be caught by the attempt above, but just in case, this catches them.
    @Listener
    public void onSpawn(SpawnEntityEvent event) {
        event.filterEntities(x -> {
            Class<? extends Entity> entityType = x.getClass();
            return checkIsValid(entityType) || isSpawnable(x.getType(), x.getWorld());
        });
    }

    // Checks to see if the entity is of a type that should spawn regardless
    private boolean checkIsValid(Class<? extends Entity> entityType) {
        return !Living.class.isAssignableFrom(entityType) || Player.class.isAssignableFrom(entityType) ||
                ArmorStand.class.isAssignableFrom(entityType);
    }

    private boolean isSpawnable(EntityType type, World world) {
        Optional<BlockSpawnsConfig> bsco = this.config.getBlockSpawnsConfigForWorld(world);
        if (!bsco.isPresent()) {
            return true;
        }

        String id = type.getId().toLowerCase();
        return !(bsco.get().isBlockVanillaMobs() && id.startsWith("minecraft:") || bsco.get().getIdsToBlock().contains(id));
    }

    @Override public void onReload() {
        this.config = getServiceUnchecked(MobConfigAdapter.class).getNodeOrDefault();
    }

    @Override public boolean shouldEnable() {
            if (Sponge.getGame().getState().ordinal() < GameState.SERVER_STARTING.ordinal()) {
                return true;
            }

            return Nucleus.getNucleus()
                .getConfigValue(MobModule.ID, MobConfigAdapter.class, MobConfig::getBlockSpawnsConfig).map(conf -> {
                    if (conf.entrySet().stream().anyMatch(x -> Sponge.getServer().getWorldProperties(x.getKey()).isPresent())) {
                        for (BlockSpawnsConfig s : conf.values()) {
                            List<String> idsToBlock = s.getIdsToBlock();
                            if (s.isBlockVanillaMobs() || Sponge.getRegistry().getAllOf(EntityType.class).stream()
                                    .anyMatch(x -> idsToBlock.contains(x.getId()))) {
                                return true;
                            }
                        }
                    }

                    return false;
                }).orElse(false);
    }
}
