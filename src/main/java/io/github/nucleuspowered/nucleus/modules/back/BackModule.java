/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.back;

import io.github.nucleuspowered.nucleus.api.service.NucleusBackService;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterService;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.back.config.BackConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.back.handlers.BackHandler;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@RegisterService(value = BackHandler.class, apiService = NucleusBackService.class)
@ModuleData(id = "back", name = "Back", softDependencies = "jail")
public class BackModule extends ConfigurableModule<BackConfigAdapter> {

    @Override
    public BackConfigAdapter createAdapter() {
        return new BackConfigAdapter();
    }

}
