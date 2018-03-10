/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.storage.api.storage;

import ninja.leaping.configurate.ConfigurationNode;

import java.util.Optional;

public interface SimpleDataProvider {

    void save(ConfigurationNode node);

    Optional<ConfigurationNode> load();

}
