/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.vanish;

import com.google.common.collect.ImmutableMap;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterService;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.internal.text.Tokens;
import io.github.nucleuspowered.nucleus.modules.vanish.config.VanishConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.vanish.service.VanishService;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

import java.util.Map;
import java.util.Optional;

@RegisterService(VanishService.class)
@ModuleData(id = "vanish", name = "Vanish")
public class VanishModule extends ConfigurableModule<VanishConfigAdapter> {

    @Override
    public VanishConfigAdapter createAdapter() {
        return new VanishConfigAdapter();
    }

    @Override protected Map<String, Tokens.Translator> tokensToRegister() {
        return ImmutableMap.<String, Tokens.Translator>builder()
                .put("vanished", new Tokens.TrueFalseVariableTranslator() {
                    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                    final Optional<Text> def = Optional.of(Text.of(TextColors.GRAY, "[Vanished]"));

                    @Override protected Optional<Text> getDefault() {
                        return this.def;
                    }

                    @Override protected boolean condition(CommandSource commandSource) {
                        return commandSource instanceof Player &&
                                Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(VanishService.class)
                                        .isVanished((Player) commandSource);
                    }
                }).build();
    }
}
