/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices.loaders;

import com.google.common.base.Preconditions;
import io.github.nucleuspowered.nucleus.dataservices.dataproviders.DataProvider;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularWorldService;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class WorldDataManager extends DataManager<UUID, ConfigurationNode, ModularWorldService> {

    public WorldDataManager(BiFunction<UUID, Boolean, DataProvider<ConfigurationNode>> dataProviderFactory, Predicate<UUID> fileExist) {
        super(dataProviderFactory, fileExist);
    }

    @Override
    protected boolean shouldNotExpire(UUID key) {
        return Sponge.getServer().getWorld(key).isPresent();
    }

    @Override
    public Optional<ModularWorldService> getNew(UUID data, DataProvider<ConfigurationNode> dataProvider) throws Exception {
        ModularWorldService m = new ModularWorldService(dataProvider,
                Sponge.getServer().getWorldProperties(data).orElseThrow(() -> new IllegalStateException("world")).getUniqueId());
        m.loadInternal();
        return Optional.of(m);
    }

    public Optional<ModularWorldService> getWorld(UUID uuid) {
        Preconditions.checkNotNull(uuid);
        return get(uuid);
    }

    public Optional<ModularWorldService> getWorld(World world) {
        Preconditions.checkNotNull(world);
        return getWorld(world.getUniqueId());
    }
}
