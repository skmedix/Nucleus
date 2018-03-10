/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.storage.flatfile;

import io.github.nucleuspowered.nucleus.storage.api.storage.SimpleDataProvider;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.Optional;

public class GeneralStorageProvider implements SimpleDataProvider {

    private final String filename;

    public GeneralStorageProvider(String filename) {
        this.filename = filename;
    }

    @Override
    public void save(ConfigurationNode node) {

    }

    @Override
    public Optional<ConfigurationNode> load() {
        return Optional.empty();
    }
}
