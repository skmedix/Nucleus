/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices.dataproviders;

import static io.github.nucleuspowered.nucleus.configurate.ConfigurateHelper.setOptions;

import com.google.common.base.Preconditions;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("WeakerAccess")
public abstract class AbstractConfigurateDataProvider<T> implements DataProvider<T> {

    private final Function<Path, ConfigurationLoader<?>> provider;

    protected final ConfigurationLoader<?> loader;
    private final Path file;
    private final boolean requiresChildren;
    private final Path backupFile;
    private final Logger logger;

    public AbstractConfigurateDataProvider(Function<Path, ConfigurationLoader<?>>  loaderProvider, Path file, boolean requiresChildren, Logger logger) {
        this.loader = loaderProvider.apply(file);
        this.provider = loaderProvider;
        this.file = file;
        this.backupFile = Paths.get(file.toAbsolutePath().toString() + ".bak");
        this.requiresChildren = requiresChildren;
        this.logger = logger;
    }

    @Override public boolean has() {
        return Files.exists(file);
    }

    @Override
    public T load() throws Exception {
        try {
            return transformOnLoad(loader.load(setOptions(getOptions())));
        } catch (Exception e) {
            return loadBackup().orElseThrow(() -> e);
        }
    }

    protected abstract T transformOnLoad(ConfigurationNode node) throws Exception;

    protected abstract ConfigurationNode transformOnSave(T info) throws Exception;

    private Optional<T> loadBackup() {
        try {
            if (Files.exists(backupFile)) {
                logger.warn("Could not load " + file.toAbsolutePath().toString() + ", attempting to load backup.");
                return Optional.of(transformOnLoad(this.provider.apply(backupFile).load(setOptions(getOptions()))));
            }
        } catch (Exception e) {
            logger.warn("Could not load " + backupFile.toAbsolutePath().toString() + " either.");
        }

        return Optional.empty();
    }

    @Override public void save(T info) throws Exception {
        Preconditions.checkNotNull(info);
        ConfigurationNode node = transformOnSave(info);
        if (node == null) {
            throw getException("Configuration Node is null.");
        } else if (node.isVirtual()) {
            throw getException("Configuration Node is virtual.");
        } else if (requiresChildren && (!node.hasMapChildren() && !node.hasListChildren())) {
            throw getException("Configuration Node has no children.");
        }

        try {
            if (Files.exists(file)) {
                Files.copy(file, backupFile, StandardCopyOption.REPLACE_EXISTING);
            }

            loader.save(node);
        } catch (IOException e) {
            if (Files.exists(backupFile)) {
                Files.copy(backupFile, file, StandardCopyOption.REPLACE_EXISTING);
            }

            throw getException(e);
        }
    }

    @Override
    public void delete() throws Exception {
        Files.delete(file);
    }

    private ConfigurationOptions getOptions() {
        return setOptions(loader.getDefaultOptions());
    }

    private IllegalStateException getException(String message) {
        return new IllegalStateException("The file " + file.getFileName() + " has not been saved.\n" + message);
    }

    private IOException getException(Throwable inner) {
        return new IOException("The file " + file.getFileName() + " has not been saved - an exception was thrown.", inner);
    }
}
