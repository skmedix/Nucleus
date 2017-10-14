/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices.modular;

import io.github.nucleuspowered.nucleus.dataservices.dataproviders.DataProvider;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.Optional;

public class ModularGeneralService extends ModularDataService<ModularGeneralService> {

    public ModularGeneralService(DataProvider<ConfigurationNode> dataProvider) throws Exception {
        super(dataProvider);
    }

    @Override protected String serviceName() {
        return "General Nucleus data";
    }

    @Override <T extends TransientModule<ModularGeneralService>> Optional<T> tryGetTransient(Class<T> module) {
        return DataModuleFactory.getTransient(module, this);
    }

    @Override <T extends DataModule<ModularGeneralService>> Optional<T> tryGet(Class<T> module) {
        return DataModuleFactory.get(module, this);
    }

}
