/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.service.NucleusPlayerMetadataService;
import io.github.nucleuspowered.nucleus.api.service.NucleusWorldUUIDChangeService;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterService;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.core.service.PlayerMetadataService;
import io.github.nucleuspowered.nucleus.modules.core.service.UUIDChangeService;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@RegisterService(value = PlayerMetadataService.class, apiService = NucleusPlayerMetadataService.class)
@RegisterService(value = UUIDChangeService.class, apiService = NucleusWorldUUIDChangeService.class)
@ModuleData(id = CoreModule.ID, name = "Core", isRequired = true)
public class CoreModule extends ConfigurableModule<CoreConfigAdapter> {

    public static final String ID = "core";

    @Override
    public CoreConfigAdapter createAdapter() {
        return new CoreConfigAdapter();
    }

    @Override
    protected void performPreTasks() throws Exception {
        super.performPreTasks();

        Nucleus.getNucleus().reloadMessages();
    }

}
