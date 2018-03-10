/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.service;

import com.google.common.collect.ImmutableMap;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.traits.InternalServiceManagerTrait;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import io.github.nucleuspowered.nucleus.util.Tuples;
import org.spongepowered.api.Sponge;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class UUIDChangeService implements Reloadable, InternalServiceManagerTrait,
        io.github.nucleuspowered.nucleus.api.service.NucleusWorldUUIDChangeService {

    private Map<UUID, UUID> mapping = ImmutableMap.of();
    private boolean canLoad = false;

    @Override public Optional<UUID> getMappedUUID(UUID oldUUID) {
        return Optional.ofNullable(this.mapping.get(oldUUID));
    }

    @Override
    @SuppressWarnings("all")
    public void onReload() throws Exception {
        if (!this.canLoad || !Nucleus.getNucleus().isServer()) {
            return;
        }

        this.mapping = getServiceUnchecked(CoreConfigAdapter.class).getNodeOrDefault().getUuidMigration()
                .entrySet().stream()
                .map(x -> {
                    try {
                        UUID u = UUID.fromString(x.getValue());
                        return new Tuples.NullableTuple<>(x.getKey(), u);
                    } catch (Exception e) {
                        return new Tuples.NullableTuple<>(x.getKey(), Sponge.getServer().getWorldProperties(x.getValue()).map(y -> y.getUniqueId()).orElse(null));
                    }
                })
                .filter(x -> x.getSecond().isPresent())
                .collect(ImmutableMap.toImmutableMap(
                        x -> x.getFirst().get(),
                        x -> x.getSecond().get()));
    }

    public void setStateAndReload() throws Exception {
        this.canLoad = true;
        onReload();
    }
}
