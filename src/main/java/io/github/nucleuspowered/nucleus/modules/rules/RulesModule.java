/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.rules;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.rules.config.RulesConfigAdapter;
import org.spongepowered.api.Sponge;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = "rules", name = "Rules")
public class RulesModule extends ConfigurableModule<RulesConfigAdapter> {

    public static final String RULES_KEY = "rules";

    @Override
    public RulesConfigAdapter createAdapter() {
        return new RulesConfigAdapter();
    }

    @Override
    protected void performPreTasks() throws Exception {
        super.performPreTasks();

        Nucleus.getNucleus().addTextFileController(
                RULES_KEY,
                Sponge.getAssetManager().getAsset(Nucleus.getNucleus(), "rules.txt").get(),
                Nucleus.getNucleus().getConfigDirPath().resolve("rules.txt"));
    }
}
