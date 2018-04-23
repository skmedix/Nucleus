/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warp;

import io.github.nucleuspowered.nucleus.api.service.NucleusWarpService;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterService;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.warp.config.WarpConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.warp.handlers.WarpHandler;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@RegisterService(value = WarpHandler.class, apiService = NucleusWarpService.class)
@ModuleData(id = "warp", name = "Warp")
public class WarpModule extends ConfigurableModule<WarpConfigAdapter> {

    @Override
    public WarpConfigAdapter createAdapter() {
        return new WarpConfigAdapter();
    }
}
