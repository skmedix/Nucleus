/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.rtp;

import io.github.nucleuspowered.nucleus.api.service.NucleusRTPService;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterService;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.rtp.config.RTPConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.rtp.service.RTPService;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@RegisterService(value = RTPService.class, apiService = NucleusRTPService.class)
@ModuleData(id = RTPModule.ID, name = "rtp")
public class RTPModule extends ConfigurableModule<RTPConfigAdapter> {

    public static final String ID = "rtp";

    @Override
    public RTPConfigAdapter createAdapter() {
        return new RTPConfigAdapter();
    }
}
