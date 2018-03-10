/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.storage.api.storage;

import io.github.nucleuspowered.nucleus.storage.api.query.Query;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface CategorisedDataProvider {

    boolean has(UUID uuid);

    List<UUID> listAll();

    void save(UUID uuid, ConfigurationNode node);

    Optional<ConfigurationNode> load(UUID uuid);

    void delete(UUID uuid);

    boolean supportsQuery(Query<?> query);

    <R, Q extends Query<R>> Map<UUID, ConfigurationNode> processQuery(Q query, R parameters);

}
