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
    private final Path backupFile;
    private final Logger logger;

    public AbstractConfigurateDataProvider(Function<Path, ConfigurationLoader<?>>  loaderProvider, Path file, Logger logger) {
        this.loader = loaderProvider.apply(file);
        this.provider = loaderProvider;
        this.file = file;
        this.backupFile = Paths.get(file.toAbsolutePath().toString() + ".bak");
        this.logger = logger;
    }

    @Override public boolean has() {
        return Files.exists(this.file);
    }

    @Override
    public T load() throws Exception {
        try {
            return transformOnLoad(this.loader.load(setOptions(getOptions())));
        } catch (Exception e) {
            return loadBackup().orElseThrow(() -> e);
        }
    }

    protected abstract T transformOnLoad(ConfigurationNode node) throws Exception;

    protected abstract ConfigurationNode transformOnSave(T info) throws Exception;

    private Optional<T> loadBackup() {
        try {
            if (Files.exists(this.backupFile)) {
                this.logger.warn("Could not load " + this.file.toAbsolutePath().toString() + ", attempting to load backup.");
                return Optional.of(transformOnLoad(this.provider.apply(this.backupFile).load(setOptions(getOptions()))));
            }
        } catch (Exception e) {
            this.logger.warn("Could not load " + this.backupFile.toAbsolutePath().toString() + " either.");
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
        }

        try {
            if (Files.exists(this.file)) {
                Files.copy(this.file, this.backupFile, StandardCopyOption.REPLACE_EXISTING);
            }

            this.loader.save(node);
        } catch (IOException e) {
            if (Files.exists(this.backupFile)) {
                Files.copy(this.backupFile, this.file, StandardCopyOption.REPLACE_EXISTING);
            }

            throw getException(e);
        }
    }

    @Override
    public void delete() throws Exception {
        Files.delete(this.file);
    }

    private ConfigurationOptions getOptions() {
        return setOptions(this.loader.getDefaultOptions());
    }

    private IllegalStateException getException(String message) {
        return new IllegalStateException("The file " + this.file.getFileName() + " has not been saved.\n" + message);
    }

    private IOException getException(Throwable inner) {
        return new IOException("The file " + this.file.getFileName() + " has not been saved - an exception was thrown.", inner);
    }
}
