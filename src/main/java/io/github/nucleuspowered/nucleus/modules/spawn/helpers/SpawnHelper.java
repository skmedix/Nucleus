/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.spawn.helpers;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Preconditions;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.modules.spawn.datamodules.SpawnWorldDataModule;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class SpawnHelper {

    private SpawnHelper() {}

    public static Transform<World> getSpawn(@Nonnull WorldProperties wp, @Nullable Player player) throws ReturnMessageException {
        Preconditions.checkNotNull(wp, "WorldProperties");
        Optional<World> ow = Sponge.getServer().getWorld(wp.getUniqueId());

        if (!ow.isPresent()) {
            throw new ReturnMessageException(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.spawn.noworld"));
        }

        return new Transform<>(ow.get(),
            wp.getSpawnPosition().toDouble().add(0.5, 0, 0.5),
            Nucleus.getNucleus().getWorldDataManager().getWorld(wp.getUniqueId()).get().get(SpawnWorldDataModule.class).getSpawnRotation()
                .orElseGet(() -> player == null ? new Vector3d(0, 0, 0) : player.getRotation()));
    }
}
