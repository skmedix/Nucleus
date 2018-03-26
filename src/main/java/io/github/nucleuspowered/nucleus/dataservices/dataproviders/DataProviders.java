/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices.dataproviders;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.configurate.ConfigurateHelper;
import io.github.nucleuspowered.nucleus.configurate.datatypes.ItemDataNode;
import io.github.nucleuspowered.nucleus.configurate.datatypes.KitConfigDataNode;
import io.github.nucleuspowered.nucleus.configurate.datatypes.UserCacheVersionNode;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class DataProviders {

    private final NucleusPlugin plugin;
    private final TypeToken<Map<String, ItemDataNode>> ttmsi = new TypeToken<Map<String, ItemDataNode>>() {};
    private final TypeToken<Map<String, String>> ttss = new TypeToken<Map<String, String>>() {};
    private final TypeToken<KitConfigDataNode> ttmk = TypeToken.of(KitConfigDataNode.class);
    private final TypeToken<UserCacheVersionNode> ttucv = TypeToken.of(UserCacheVersionNode.class);

    private final String userJson = "userdata%1$s%2$s%1$s%3$s.json";
    private final String worldJson = "worlddata%1$s%2$s%1$s%3$s.json";

    public DataProviders(NucleusPlugin plugin) {
        this.plugin = plugin;
    }

    public DataProvider<ConfigurationNode> getUserFileDataProviders(UUID uuid, boolean create) {
        // For now, just the Configurate one.
        try {
            Path p = getFile(userJson, uuid);
            if (create || doesUserFileExist(uuid)) {
                return new SimpleConfigurateDataProvider(path -> getGsonBuilder().setPath(path).build(), p, plugin.getLogger());
            }
        } catch (Exception e) {
            // ignored
        }

        return null;
    }

    public boolean doesUserFileExist(UUID uuid) {
        try {
            return Files.exists(getFile(userJson, uuid));
        } catch (Exception e) {
            return false;
        }
    }

    public DataProvider<ConfigurationNode> getWorldFileDataProvider(UUID uuid, boolean create) {
        // For now, just the Configurate one.
        try {
            Path p = getFile(worldJson, uuid);
            if (create || doesWorldFileExist(uuid)) {
                return new SimpleConfigurateDataProvider(path -> getGsonBuilder().setPath(path).build(), p, plugin.getLogger());
            }
        } catch (Exception e) {
            // ignored
        }

        return null;
    }

    public boolean doesWorldFileExist(UUID uuid) {
        try {
            return Files.exists(getFile(worldJson, uuid));
        } catch (Exception e) {
            return false;
        }
    }

    public DataProvider.FileChanging<KitConfigDataNode> getKitsDataProvider() {
        // For now, just the Configurate one.
        try {
            Supplier<Path> p = () -> plugin.getDataPath().resolve("kits.json");
            return new FileChangingConfigurateDataProvider<>(
                    ttmk,
                    path -> new LazyConfigurationLoader<>(() -> getGsonBuilder().setPath(path).build()),
                    p);
        } catch (Exception e) {
            return null;
        }
    }

    public DataProvider.FileChanging<UserCacheVersionNode> getUserCacheDataProvider() {
        try {
            Supplier<Path> p = () -> plugin.getDataPath().resolve("nucleususercache.json");
            return new FileChangingConfigurateDataProvider<>(ttucv,
                    path -> new LazyConfigurationLoader<>(() -> getGsonBuilder().setPath(path).build()), p);
        } catch (Exception e) {
            return null;
        }
    }


    public DataProvider.FileChanging<ConfigurationNode> getGeneralDataProvider() {
        // For now, just the Configurate one.
        try {
            Supplier<Path> p = () -> plugin.getDataPath().resolve("general.json");
            return new FileChangingSimpleConfigurateDataProvider(
                    path -> new LazyConfigurationLoader<>(() -> getGsonBuilder().setPath(path).build()),
                    p,
                    plugin.getLogger());
        } catch (Exception e) {
            return null;
        }
    }

    public DataProvider<Map<String, ItemDataNode>> getItemDataProvider() {
        // For now, just the Configurate one.
        try {
            Path p = plugin.getConfigDirPath().resolve("items.conf");
            return new ConfigurateDataProvider<>(ttmsi, path -> new LazyConfigurationLoader<>(() -> getHoconBuilder().setPath(path).build()), HashMap::new, p,

                    plugin.getLogger());
        } catch (Exception e) {
            return null;
        }
    }

    public DataProvider.FileChanging<Map<String, String>> getNameBanDataProvider() {
        // For now, just the Configurate one.
        try {
            Supplier<Path> p = () -> plugin.getDataPath().resolve("namebans.json");
            return new FileChangingConfigurateDataProvider<>(ttss, path -> new LazyConfigurationLoader<>(
                    () -> getGsonBuilder().setPath(path).build()),
                    HashMap::new,
                    p
            );
        } catch (Exception e) {
            return null;
        }
    }

    private Path getFile(String template, UUID uuid) throws Exception {
        String u = uuid.toString();
        String f = u.substring(0, 2);
        return getFile(plugin.getDataPath().resolve(String.format(template, File.separator, f, u)));
    }

    private Path getFile(Path file) throws Exception {
        if (Files.notExists(file)) {
            Files.createDirectories(file.getParent());
        }

        return file;
    }

    private GsonConfigurationLoader.Builder getGsonBuilder() {
        GsonConfigurationLoader.Builder gsb = GsonConfigurationLoader.builder();
        return gsb.setDefaultOptions(ConfigurateHelper.setOptions(gsb.getDefaultOptions()));
    }

    private HoconConfigurationLoader.Builder getHoconBuilder() {
        HoconConfigurationLoader.Builder gsb = HoconConfigurationLoader.builder();
        return gsb.setDefaultOptions(ConfigurateHelper.setOptions(gsb.getDefaultOptions()));
    }

    /**
     * Only performs the loading when required.
     * @param <T> The type of node that this lazy loaded loader will load.
     */
    private static class LazyConfigurationLoader<T extends ConfigurationNode> implements ConfigurationLoader<T> {

        private ConfigurationLoader<T> lazyLoad = null;
        private final Supplier<ConfigurationLoader<T>> supplier;

        private LazyConfigurationLoader(Supplier<ConfigurationLoader<T>> supplier) {
            Preconditions.checkNotNull(supplier);
            this.supplier = supplier;
        }

        @Override
        public ConfigurationOptions getDefaultOptions() {
            init();
            return lazyLoad.getDefaultOptions();
        }

        @Override
        public T load(ConfigurationOptions options) throws IOException {
            init();
            return lazyLoad.load(options);
        }

        @Override
        public void save(ConfigurationNode node) throws IOException {
            init();
            lazyLoad.save(node);
        }

        @Override
        public T createEmptyNode(ConfigurationOptions options) {
            init();
            return lazyLoad.createEmptyNode(options);
        }

        private void init() {
            if (lazyLoad == null) {
                lazyLoad = supplier.get();
            }
        }
    }
}
