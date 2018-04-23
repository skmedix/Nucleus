/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.playerinfo;

import io.github.nucleuspowered.nucleus.api.service.NucleusSeenService;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterService;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.afk.AFKModule;
import io.github.nucleuspowered.nucleus.modules.playerinfo.config.PlayerInfoConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.playerinfo.handlers.SeenHandler;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@RegisterService(value = SeenHandler.class, apiService = NucleusSeenService.class)
@ModuleData(id = PlayerInfoModule.ID, name = "Player Info", softDependencies = AFKModule.ID)
public class PlayerInfoModule extends ConfigurableModule<PlayerInfoConfigAdapter> {

    public static final String ID = "playerinfo";

    @Override
    public PlayerInfoConfigAdapter createAdapter() {
        return new PlayerInfoConfigAdapter();
    }

}
