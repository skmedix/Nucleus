/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.nickname;

import io.github.nucleuspowered.nucleus.api.service.NucleusNicknameService;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.nickname.config.NicknameConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.nickname.services.NicknameService;
import org.spongepowered.api.Sponge;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = NicknameModule.ID, name = "Nickname")
public class NicknameModule extends ConfigurableModule<NicknameConfigAdapter> {

    public final static String ID = "nickname";

    @Override
    protected void performPreTasks() throws Exception {
        super.performPreTasks();
        NicknameService nns = new NicknameService();
        nns.onReload();
        plugin.registerReloadable(nns::onReload);
        plugin.getInternalServiceManager().registerService(NicknameService.class, nns);
        Sponge.getServiceManager().setProvider(plugin, NucleusNicknameService.class, nns);
        nns.onReload();
    }

    @Override public void postEnable() {
        plugin.getInternalServiceManager().getServiceUnchecked(NicknameService.class).register();
    }

    @Override
    public NicknameConfigAdapter createAdapter() {
        return new NicknameConfigAdapter();
    }
}
