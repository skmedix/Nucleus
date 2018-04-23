/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.home.datamodules;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Home;
import io.github.nucleuspowered.nucleus.configurate.datatypes.LocationNode;
import io.github.nucleuspowered.nucleus.dataservices.modular.DataKey;
import io.github.nucleuspowered.nucleus.dataservices.modular.DataModule;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import io.github.nucleuspowered.nucleus.internal.LocationData;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HomeUserDataModule extends DataModule.ReferenceService<ModularUserService> {

    @DataKey("homes")
    private Map<String, LocationNode> homeData = Maps.newHashMap();

    public HomeUserDataModule(ModularUserService modularDataService) {
        super(modularDataService);
    }

    public Optional<Home> getHome(String home) {
        if (this.homeData == null) {
            return Optional.empty();
        }

        LocationNode ln = Util.getValueIgnoreCase(this.homeData, home).orElse(null);
        if (ln != null) {
            return Optional.of(new HomeData(home, ln.getWorld(), ln.getPosition(), ln.getRotation()));
        }

        return Optional.empty();
    }


    public Map<String, Home> getHomes() {
        if (this.homeData == null || this.homeData.isEmpty()) {
            return Maps.newHashMap();
        }

        return this.homeData.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        x -> new HomeData(x.getKey(), x.getValue().getWorld(), x.getValue().getPosition(), x.getValue().getRotation())));
    }

    public boolean setHome(String home, Location<World> location, Vector3d rotation) {
        return setHome(home, location, rotation, false);
    }

    public boolean setHome(String home, Location<World> location, Vector3d rotation, boolean overwrite) {
        final Pattern warpName = Pattern.compile("^[a-zA-Z][a-zA-Z0-9]{1,15}$");

        if (this.homeData == null) {
            this.homeData = Maps.newHashMap();
        }

        Optional<String> os = Util.getKeyIgnoreCase(this.homeData, home);
        if (os.isPresent() || !warpName.matcher(home).matches()) {
            if (!overwrite || !deleteHome(home)) {
                return false;
            }
        }

        this.homeData.put(home, new LocationNode(location, rotation));
        return true;
    }

    public boolean deleteHome(String home) {
        if (this.homeData == null) {
            return false;
        }

        Optional<String> os = Util.getKeyIgnoreCase(this.homeData, home);
        if (os.isPresent()) {
            this.homeData.remove(os.get());
            return true;
        }

        return false;
    }

    private class HomeData extends LocationData implements Home {

        private HomeData(String name, UUID world, Vector3d position, Vector3d rotation) {
            super(name, world, position, rotation);
        }

        public UUID getOwnersUniqueId() {
            return HomeUserDataModule.this.getService().getUniqueId();
        }
    }
}
