/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.back.datamodules;

import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import io.github.nucleuspowered.nucleus.dataservices.modular.TransientModule;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.world.World;

import java.util.Optional;

import javax.annotation.Nullable;

public class BackUserTransientModule extends TransientModule<ModularUserService> {

    @Nullable
    private Transform<World> lastLocation;

    private boolean logLastLocation = true;

    public Optional<Transform<World>> getLastLocation() {
        return Optional.ofNullable(this.lastLocation);
    }

    public void setLastLocation(@Nullable Transform<World> location) {
        this.lastLocation = location;
    }

    public boolean isLogLastLocation() {
        return this.logLastLocation;
    }

    public void setLogLastLocation(boolean logLastLocation) {
        this.logLastLocation = logLastLocation;
    }
}
