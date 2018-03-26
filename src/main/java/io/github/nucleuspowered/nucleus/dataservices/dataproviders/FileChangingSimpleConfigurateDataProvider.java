/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices.dataproviders;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Supplier;

class FileChangingSimpleConfigurateDataProvider implements DataProvider.FileChanging<ConfigurationNode> {

    private final Supplier<SimpleConfigurateDataProvider> providerSupplier;
    private SimpleConfigurateDataProvider provider;

    @SuppressWarnings("unchecked") FileChangingSimpleConfigurateDataProvider(Function<Path, ConfigurationLoader<?>> loaderProvider, Supplier<Path> file, Logger logger) {
       this.providerSupplier = () -> new SimpleConfigurateDataProvider(loaderProvider, file.get(), logger);
    }

    public void onChange() {
        this.provider = this.providerSupplier.get();
    }

    @Override
    public boolean has() {
        return this.provider.has();
    }

    @Override
    public ConfigurationNode load() throws Exception {
        return this.provider.load();
    }

    @Override
    public void save(ConfigurationNode info) throws Exception {
        this.provider.save(info);
    }

    @Override
    public void delete() throws Exception {
        this.provider.delete();
    }
}
