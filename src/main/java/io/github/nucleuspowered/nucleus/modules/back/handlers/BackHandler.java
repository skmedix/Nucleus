/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.back.handlers;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.service.NucleusBackService;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import io.github.nucleuspowered.nucleus.modules.back.datamodules.BackUserTransientModule;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.world.World;

import java.util.Optional;

@SuppressWarnings("deprecation")
public class BackHandler implements NucleusBackService {

    private final UserDataManager loader = Nucleus.getNucleus().getUserDataManager();

    @Override
    public Optional<Transform<World>> getLastLocation(User user) {
        Optional<ModularUserService> oi = this.loader.getUser(user);
        return oi.flatMap(modularUserService -> modularUserService.getTransient(BackUserTransientModule.class).getLastLocation());

    }

    @Override
    public void setLastLocation(User user, Transform<World> location) {
        this.loader.getUser(user).ifPresent(x -> x.getTransient(BackUserTransientModule.class).setLastLocation(location));
    }

    @Override
    public void removeLastLocation(User user) {
        this.loader.getUser(user).ifPresent(x -> x.getTransient(BackUserTransientModule.class).setLastLocation(null));
    }

    @Override
    public boolean isLoggingLastLocation(User user) {
        Optional<ModularUserService> oi = this.loader.getUser(user);
        return oi.isPresent() && oi.get().getTransient(BackUserTransientModule.class).isLogLastLocation();
    }

    @Override
    public void setLoggingLastLocation(User user, boolean log) {
        this.loader.getUser(user).ifPresent(x -> x.getTransient(BackUserTransientModule.class).setLogLastLocation(log));
    }
}
