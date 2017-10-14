/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices.modular;

import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.dataservices.dataproviders.DataProvider;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.Optional;
import java.util.UUID;

public class ModularWorldService extends ModularDataService<ModularWorldService> {

    private final UUID worldUUID;
    private final NucleusPlugin plugin;

    public ModularWorldService(DataProvider<ConfigurationNode> dataProvider, NucleusPlugin plugin, UUID worldUUID) throws Exception {
        super(dataProvider);
        this.worldUUID = worldUUID;
        this.plugin = plugin;
    }

    public UUID getWorldUUID() {
        return this.worldUUID;
    }

    @Override protected String serviceName() {
        return "Nucleus Data for world " + this.worldUUID.toString() + " (" + Sponge.getServer().getWorldProperties(this.worldUUID)
                .map(WorldProperties::getWorldName).orElse("unknown") + ")";
    }

    @Override <T extends TransientModule<ModularWorldService>> Optional<T> tryGetTransient(Class<T> module) {
        return DataModuleFactory.getTransient(module, this);
    }

    @Override <T extends DataModule<ModularWorldService>> Optional<T> tryGet(Class<T> module) {
        return DataModuleFactory.get(module, this);
    }
}
