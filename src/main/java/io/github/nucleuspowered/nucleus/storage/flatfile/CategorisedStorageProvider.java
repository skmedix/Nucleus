/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.storage.flatfile;

import com.google.common.collect.ImmutableMap;
import io.github.nucleuspowered.nucleus.storage.api.query.Query;
import io.github.nucleuspowered.nucleus.storage.api.storage.CategorisedDataProvider;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class CategorisedStorageProvider implements CategorisedDataProvider {

    private final String location;

    public CategorisedStorageProvider(String location) {
        this.location = location;
    }

    @Override
    public boolean has(UUID uuid) {
        return false;
    }

    @Override
    public List<UUID> listAll() {
        return null;
    }

    @Override
    public void save(UUID uuid, ConfigurationNode node) {

    }

    @Override
    public Optional<ConfigurationNode> load(UUID uuid) {
        return Optional.empty();
    }

    @Override
    public void delete(UUID uuid) {

    }

    @Override public boolean supportsQuery(Query<?> query) {
        return false;
    }

    @Override public <R, Q extends Query<R>> Map<UUID, ConfigurationNode> processQuery(Q query, R parameters) {
        return ImmutableMap.of();
    }
}
