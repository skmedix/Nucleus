/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.spawn;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.spawn.config.SpawnConfigAdapter;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = SpawnModule.ID, name = "Spawn")
public class SpawnModule extends ConfigurableModule<SpawnConfigAdapter> {

    public static final String ID = "spawn";

    @Override
    public SpawnConfigAdapter createAdapter() {
        return new SpawnConfigAdapter();
    }

    @Override protected void setPermissionPredicates() {
        Nucleus.getNucleus().getPermissionResolver().registerPermissionPredicate(
                perm -> perm.toLowerCase().startsWith("nucleus.spawn.worlds."), SuggestedLevel.ADMIN
        );
    }
}
