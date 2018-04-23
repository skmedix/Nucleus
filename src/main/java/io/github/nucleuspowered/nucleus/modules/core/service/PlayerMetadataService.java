/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.service;

import com.flowpowered.math.vector.Vector3d;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.service.NucleusPlayerMetadataService;
import io.github.nucleuspowered.nucleus.configurate.datatypes.LocationNode;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import io.github.nucleuspowered.nucleus.modules.core.datamodules.CoreUserDataModule;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

@NonnullByDefault
public class PlayerMetadataService implements NucleusPlayerMetadataService {

    @Override public Optional<Result> getUserData(UUID uuid) {
        return Nucleus.getNucleus().getUserDataManager().get(uuid, false)
            .map(ResultImpl::new);
    }

    public class ResultImpl implements Result {

        private final User user;

        @Nullable private final Instant login;
        @Nullable private final Instant logout;
        @Nullable private final String lastIP;
        @Nullable private final LocationNode lastLocation;

        private ResultImpl(ModularUserService userService) {
            this.user = userService.getUser();

            CoreUserDataModule core = userService.get(CoreUserDataModule.class);
            this.login = core.getLastLogin().orElse(null);
            this.logout = core.getLastLogout().orElse(null);
            this.lastIP = core.getLastIp().orElse(null);
            this.lastLocation = core.getLogoutLocationSafe().orElse(null);
        }

        @Override public Optional<Instant> getLastLogin() {
            return Optional.ofNullable(this.login);
        }

        @Override public Optional<Instant> getLastLogout() {
            return Optional.ofNullable(this.logout);
        }

        @Override public Optional<String> getLastIP() {
            return Optional.ofNullable(this.lastIP);
        }

        @Override public Optional<Tuple<WorldProperties, Vector3d>> getLastLocation() {
            Optional<Player> pl = this.user.getPlayer();
            if (pl.isPresent()) {
                Location<World> l = pl.get().getLocation();
                return Optional.of(Tuple.of(
                    l.getExtent().getProperties(),
                    l.getPosition()
                ));
            }

            try {
                if (this.lastLocation != null) {
                    return Optional.of(this.lastLocation.getLocationIfNotLoaded());
                }
            } catch (Exception ignored) {}

            return Optional.empty();
        }
    }
}
