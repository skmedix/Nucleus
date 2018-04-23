/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mute;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.service.NucleusMuteService;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterService;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.internal.text.Tokens;
import io.github.nucleuspowered.nucleus.modules.mute.commands.CheckMuteCommand;
import io.github.nucleuspowered.nucleus.modules.mute.config.MuteConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.mute.data.MuteData;
import io.github.nucleuspowered.nucleus.modules.mute.handler.MuteHandler;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

import java.util.Map;
import java.util.Optional;

@RegisterService(value = MuteHandler.class, apiService = NucleusMuteService.class)
@ModuleData(id = MuteModule.ID, name = "Mute")
public class MuteModule extends ConfigurableModule<MuteConfigAdapter> {

    public static final String ID = "mute";

    @Override
    public MuteConfigAdapter createAdapter() {
        return new MuteConfigAdapter();
    }

    @Override
    public void performEnableTasks() {
        createSeenModule(CheckMuteCommand.class, (c, u) -> {

            // If we have a ban service, then check for a ban.
            MuteHandler jh = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(MuteHandler.class);
            if (jh.isMuted(u)) {
                MuteData jd = jh.getPlayerMuteData(u).get();
                // Lightweight checkban.
                Text.Builder m;
                if (jd.getRemainingTime().isPresent()) {
                    m =
                            Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("seen.ismuted.temp", Util.getTimeToNow(jd.getEndTimestamp().get())).toBuilder();
                } else {
                    m = Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("seen.ismuted.perm").toBuilder();
                }

                return Lists.newArrayList(
                        m.onClick(TextActions.runCommand("/checkmute " + u.getName()))
                                .onHover(TextActions.showText(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("standard.clicktoseemore"))).build(),
                        Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("standard.reason", jd.getReason()));
            }

            return Lists.newArrayList(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("seen.notmuted"));
        });
    }

    @Override protected Map<String, Tokens.Translator> tokensToRegister() {
        return ImmutableMap.<String, Tokens.Translator>builder()
                .put("muted", new Tokens.TrueFalseVariableTranslator() {
                    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                    final Optional<Text> def = Optional.of(Text.of(TextColors.GRAY, "[Muted]"));

                    @Override protected Optional<Text> getDefault() {
                        return this.def;
                    }

                    @Override protected boolean condition(CommandSource commandSource) {
                        return commandSource instanceof Player &&
                                Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(MuteHandler.class).isMuted((Player) commandSource);
                    }
                })
                .build();
    }
}
