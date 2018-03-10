/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.storage.flatfile;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.storage.api.storage.CategorisedDataProvider;
import io.github.nucleuspowered.nucleus.storage.api.storage.SimpleDataProvider;
import io.github.nucleuspowered.nucleus.storage.api.storage.StorageService;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Supplier;

public class FlatFileStorageService implements StorageService {

    private static final String USER_LOCATION = "userdata%1$s%2$s%1$s%3$s.json";
    private static final String WORLD_LOCATION = "worlddata%1$s%2$s%1$s%3$s.json";

    private final SimpleDataProvider general = new GeneralStorageProvider("general.json");
    private final SimpleDataProvider kit = new GeneralStorageProvider("kits.json");
    private final CategorisedDataProvider user = new CategorisedStorageProvider(USER_LOCATION);
    private final CategorisedDataProvider world = new CategorisedStorageProvider(WORLD_LOCATION);

    @Override
    public void register(Supplier<ConfigurationNode> newNodeSupplier) {

    }

    @Override
    public CommentedConfigurationNode provideServiceConfig() {
        return SimpleCommentedConfigurationNode.root();
    }

    @Override
    public SimpleDataProvider generalData() {
        return this.general;
    }

    @Override
    public SimpleDataProvider kitData() {
        return this.kit;
    }

    @Override
    public CategorisedDataProvider userData() {
        return this.user;
    }

    @Override
    public CategorisedDataProvider worldData() {
        return this.world;
    }

    private Path getFile(String template, UUID uuid) throws Exception {
        String u = uuid.toString();
        String f = u.substring(0, 2);
        return getFile(Nucleus.getNucleus().getDataPath().resolve(String.format(template, File.separator, f, u)));
    }

    private Path getFile(Path file) throws Exception {
        if (Files.notExists(file)) {
            Files.createDirectories(file.getParent());
        }

        return file;
    }

}
