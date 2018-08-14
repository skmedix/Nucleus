/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit;

import io.github.nucleuspowered.nucleus.api.service.NucleusKitService;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterService;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionResolverImpl;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.kit.config.KitConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.kit.handlers.KitHandler;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@RegisterService(value = KitHandler.class, apiService = NucleusKitService.class)
@ModuleData(id = KitModule.ID, name = "Kit")
public class KitModule extends ConfigurableModule<KitConfigAdapter> {

    public static final String ID = "kit";

    @Override
    public KitConfigAdapter createAdapter() {
        return new KitConfigAdapter();
    }

    @Override protected void setPermissionPredicates() {
        PermissionResolverImpl.INSTANCE.registerPermissionPredicate(perm -> perm.toLowerCase().startsWith("nucleus.kits."), SuggestedLevel.ADMIN);
    }
}
