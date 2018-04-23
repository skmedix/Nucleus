/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.servershop;

import io.github.nucleuspowered.nucleus.api.service.NucleusServerShopService;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterService;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.servershop.config.ServerShopConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.servershop.services.ItemWorthService;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@RegisterService(value = ItemWorthService.class, apiService = NucleusServerShopService.class)
@ModuleData(id = ServerShopModule.ID, name = "Server Shop")
public class ServerShopModule extends ConfigurableModule<ServerShopConfigAdapter> {

    public static final String ID = "server-shop";

    @Override
    public ServerShopConfigAdapter createAdapter() {
        return new ServerShopConfigAdapter();
    }

}
