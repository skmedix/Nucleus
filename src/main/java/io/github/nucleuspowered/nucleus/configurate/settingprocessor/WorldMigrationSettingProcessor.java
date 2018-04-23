/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.configurate.settingprocessor;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.neutrino.settingprocessor.SettingProcessor;
import io.github.nucleuspowered.nucleus.internal.traits.InternalServiceManagerTrait;
import io.github.nucleuspowered.nucleus.modules.core.service.UUIDChangeService;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.util.UUID;

public class WorldMigrationSettingProcessor implements SettingProcessor, InternalServiceManagerTrait {

    private final static TypeToken<UUID> uuidTypeToken = TypeToken.of(UUID.class);

    @Override
    public void process(ConfigurationNode cn) {
        try {
            UUID uuid = cn.getValue(uuidTypeToken);
            getServiceUnchecked(UUIDChangeService.class).getMappedUUID(uuid)
                    .ifPresent(x -> {
                        try {
                            cn.setValue(uuidTypeToken, x);
                        } catch (ObjectMappingException e) {
                            // Don't bother
                        }
                    });
        } catch (Exception e) {
            // Swallow
        }
    }
}
