/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk;

import com.google.common.collect.ImmutableMap;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.service.NucleusAFKService;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommandInterceptors;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterService;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.internal.text.Tokens;
import io.github.nucleuspowered.nucleus.modules.afk.config.AFKConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.afk.handlers.AFKHandler;
import io.github.nucleuspowered.nucleus.modules.afk.interceptors.AFKCommandInterceptor;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

import java.util.Map;
import java.util.Optional;

@RegisterService(value = AFKHandler.class, apiService = NucleusAFKService.class)
@RegisterCommandInterceptors(AFKCommandInterceptor.class)
@ModuleData(id = AFKModule.ID, name = "AFK")
public class AFKModule extends ConfigurableModule<AFKConfigAdapter> {

    public static final String ID = "afk";

    @Override
    public AFKConfigAdapter createAdapter() {
        return new AFKConfigAdapter();
    }

    @Override protected Map<String, Tokens.Translator> tokensToRegister() {
        return ImmutableMap.<String, Tokens.Translator>builder()
                .put("afk", new Tokens.TrueFalseVariableTranslator() {
                    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                    final Optional<Text> def = Optional.of(Text.of(TextColors.GRAY, "[AFK]"));

                    @Override protected Optional<Text> getDefault() {
                        return this.def;
                    }

                    @Override protected boolean condition(CommandSource source) {
                        return source instanceof Player && Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(AFKHandler.class).isAFK((Player) source);
                    }
                })
                .build();
    }
}
