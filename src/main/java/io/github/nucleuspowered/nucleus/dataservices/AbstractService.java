/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices;

import com.google.common.base.Preconditions;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.dataservices.dataproviders.DataProvider;

public abstract class AbstractService<T> implements Service {

    protected T data;
    private final DataProvider<T> dataProvider;

    protected AbstractService(DataProvider<T> dataProvider) {
        this.dataProvider = Preconditions.checkNotNull(dataProvider);
    }

    public final void changeFile() {
        if (this.dataProvider instanceof DataProvider.FileChanging) {
            ((DataProvider.FileChanging) this.dataProvider).onChange();
        }
    }

    public final boolean isLoaded() {
        return this.data != null;
    }

    protected abstract String serviceName();

    @Override
    public boolean load() {
        try {
            loadInternal();
            return true;
        } catch (Exception e) {
            Nucleus.getNucleus().getLogger().error("Could not load", e);

            return false;
        }
    }

    @Override
    public void loadInternal() throws Exception {
        if (Nucleus.getNucleus().isPrintingSavesAndLoads()) {
            Nucleus.getNucleus().getLogger().info("Loading: " + serviceName());
        }
        this.data = this.dataProvider.load();
    }

    @Override
    public boolean save() {
        try {
            saveInternal();
            return true;
        } catch (Exception e) {
            Nucleus.getNucleus().getLogger().error("Could not save", e);
            return false;
        }
    }

    @Override
    public void saveInternal() throws Exception {
        if (this.data != null) {
            if (Nucleus.getNucleus().isPrintingSavesAndLoads()) {
                Nucleus.getNucleus().getLogger().info("Saving: " + serviceName());
            }
            this.dataProvider.save(this.data);
            return;
        }

        throw new IllegalStateException("Data has not been initialised.");
    }

    @Override public boolean delete() {
        try {
            if (Nucleus.getNucleus().isPrintingSavesAndLoads()) {
                Nucleus.getNucleus().getLogger().info("Deleting: " + serviceName());
            }
            this.dataProvider.delete();
            return true;
        } catch (Exception e) {
            Nucleus.getNucleus().getLogger().error("Could not delete", e);

            return false;
        }
    }
}
