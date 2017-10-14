/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices.loaders;

import com.google.common.base.Preconditions;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
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

    public WorldDataManager(NucleusPlugin plugin, BiFunction<UUID, Boolean, DataProvider<ConfigurationNode>> dataProviderFactory,
            Predicate<UUID> fileExist) {
        super(plugin, dataProviderFactory, fileExist);
    }

    @Override
    public Optional<ModularWorldService> getNew(UUID data, DataProvider<ConfigurationNode> dataProvider) throws Exception {
        ModularWorldService m = new ModularWorldService(dataProvider, plugin,
                Sponge.getServer().getWorldProperties(data).orElseThrow(() -> new IllegalStateException("world")).getUniqueId());
        m.loadInternal();
        return Optional.of(m);
    }

    public Optional<ModularWorldService> getWorld(UUID uuid) {
        Preconditions.checkNotNull(uuid);
        return Optional.ofNullable(get(uuid).orElse(null));
    }

    public Optional<ModularWorldService> getWorld(World world) {
        Preconditions.checkNotNull(world);
        return getWorld(world.getUniqueId());
    }
}
