/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.nickname;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.service.NucleusNicknameService;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterService;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.nickname.config.NicknameConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.nickname.services.NicknameService;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@RegisterService(value = NicknameService.class, apiService = NucleusNicknameService.class)
@ModuleData(id = NicknameModule.ID, name = "Nickname")
public class NicknameModule extends ConfigurableModule<NicknameConfigAdapter> {

    public final static String ID = "nickname";

    @Override
    public void performPostTasks() {
        Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(NicknameService.class).register();
    }

    @Override
    public NicknameConfigAdapter createAdapter() {
        return new NicknameConfigAdapter();
    }
}
