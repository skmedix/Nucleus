/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.storage.api.storage;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

import java.util.function.Supplier;

public interface StorageService {

    /**
     * Called by Nucleus when the storage service is being registered with it.
     * It provides a supplier that you should use to create your
     * {@link ConfigurationNode}s.
     *
     * @param newNodeSupplier The supplier
     */
    void register(Supplier<ConfigurationNode> newNodeSupplier);

    /**
     * Provides the default configuration for your service.
     *
     * @return The configuration
     */
    CommentedConfigurationNode provideServiceConfig();

    SimpleDataProvider generalData();

    SimpleDataProvider kitData();

    CategorisedDataProvider userData();

    CategorisedDataProvider worldData();
}
