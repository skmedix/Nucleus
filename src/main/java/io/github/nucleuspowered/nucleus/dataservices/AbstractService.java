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

    protected AbstractService(DataProvider<T> dataProvider) throws Exception {
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

    @Override public boolean load() {
        try {
            loadInternal();
            return true;
        } catch (Exception e) {
            Nucleus.getNucleus().getLogger().error("Could not load", e);

            return false;
        }
    }

    @Override public void loadInternal() throws Exception {
        if (Nucleus.getNucleus().isPrintingSavesAndLoads()) {
            Nucleus.getNucleus().getLogger().info("Loading: " + serviceName());
        }
        data = dataProvider.load();
    }

    @Override public boolean save() {
        if (data != null) {
            try {
                if (Nucleus.getNucleus().isPrintingSavesAndLoads()) {
                    Nucleus.getNucleus().getLogger().info("Saving: " + serviceName());
                }
                dataProvider.save(data);
                return true;
            } catch (Exception e) {
                Nucleus.getNucleus().getLogger().error("Could not save", e);

                return false;
            }
        }

        return true;
    }

    @Override public boolean delete() {
        try {
            if (Nucleus.getNucleus().isPrintingSavesAndLoads()) {
                Nucleus.getNucleus().getLogger().info("Deleting: " + serviceName());
            }
            dataProvider.delete();
            return true;
        } catch (Exception e) {
            Nucleus.getNucleus().getLogger().error("Could not delete", e);

            return false;
        }
    }
}
