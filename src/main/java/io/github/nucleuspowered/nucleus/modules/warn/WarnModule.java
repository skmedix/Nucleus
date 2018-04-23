/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warn;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.service.NucleusWarningService;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterService;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.warn.commands.CheckWarningsCommand;
import io.github.nucleuspowered.nucleus.modules.warn.config.WarnConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.warn.handlers.WarnHandler;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@RegisterService(value = WarnHandler.class, apiService = NucleusWarningService.class)
@ModuleData(id = WarnModule.ID, name = "Warn")
public class WarnModule extends ConfigurableModule<WarnConfigAdapter> {

    public static final String ID = "warn";

    @Override
    public WarnConfigAdapter createAdapter() {
        return new WarnConfigAdapter();
    }

    @Override
    public void performEnableTasks() {
        // Take base permission from /checkwarnings.
        createSeenModule(CheckWarningsCommand.class, (c, u) -> {

            WarnHandler jh = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(WarnHandler.class);
            int active = jh.getWarningsInternal(u, true, false).size();
            int expired = jh.getWarningsInternal(u, false, true).size();

            Text r = Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("seen.warnings", String.valueOf(active), String.valueOf(expired));
            if (active > 0) {
                return Lists.newArrayList(
                        r.toBuilder().onClick(TextActions.runCommand("/checkwarnings " + u.getName()))
                                .onHover(TextActions.showText(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("standard.clicktoseemore"))).build());
            }

            return Lists.newArrayList(r);
        });
    }
}
