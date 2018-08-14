/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mob;

import io.github.nucleuspowered.nucleus.internal.permissions.PermissionResolverImpl;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.mob.config.MobConfigAdapter;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = MobModule.ID, name = "Mob")
public class MobModule extends ConfigurableModule<MobConfigAdapter> {

    public final static String ID = "mob";

    @Override
    public MobConfigAdapter createAdapter() {
        return new MobConfigAdapter();
    }

    @Override
    protected void setPermissionPredicates() {
        PermissionResolverImpl.INSTANCE.registerPermissionPredicate(perm -> perm.toLowerCase().startsWith("nucleus.spawnmob.mobs."), SuggestedLevel.ADMIN);
    }
}
