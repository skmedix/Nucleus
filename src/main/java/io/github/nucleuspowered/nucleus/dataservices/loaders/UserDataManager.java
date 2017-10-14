/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices.loaders;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.dataservices.dataproviders.DataProvider;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import ninja.leaping.configurate.ConfigurationNode;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UserDataManager extends DataManager<UUID, ConfigurationNode, ModularUserService> {

    public UserDataManager(NucleusPlugin plugin, BiFunction<UUID, Boolean, DataProvider<ConfigurationNode>> dataProviderFactory,
            Predicate<UUID> fileExist) {
        super(plugin, dataProviderFactory, fileExist);
    }

    public ModularUserService getUnchecked(UUID user) {
        return get(user).orElseThrow(NullPointerException::new);
    }

    public ModularUserService getUnchecked(User user) {
        return get(user).orElseThrow(NullPointerException::new);
    }

    public Optional<ModularUserService> get(User user) {
        return get(user.getUniqueId());
    }

    public Optional<ModularUserService> get(User user, boolean create) {
        return get(user.getUniqueId(), create);
    }

    @Override
    public Optional<ModularUserService> getNew(UUID uuid, DataProvider<ConfigurationNode> dataProvider) throws Exception {
        // Does the user exist?
        Optional<User> user = Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(uuid);
        if (user.isPresent()) {
            int trace = Nucleus.getNucleus().traceUserCreations();
            if (trace > 0) {
                boolean t = trace >= 2 || !Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(uuid).isPresent()
                        || Sponge.getServer().getPlayer(uuid).map(x -> x.getClass().getSimpleName().toLowerCase().contains("fakeplayer")).orElse(true);

                if (t) {
                    Logger logger = Nucleus.getNucleus().getLogger();
                    logger.info("Creating user: " + uuid.toString());
                    Sponge.getServer().getPlayer(uuid).ifPresent(x -> logger.info("Player Class: " + x.getClass().getName()));
                    logger.info("THIS IS NOT AN ERROR", new Throwable());
                }
            }

            ModularUserService m = new ModularUserService(dataProvider, user.get().getUniqueId());
            m.loadInternal();
            return Optional.of(m);
        }

        return Optional.empty();
    }

    public void removeOfflinePlayers() {
        removeOfflinePlayers(false);
    }

    public void removeOfflinePlayers(boolean allOffline) {
        saveAll();

        // If allOffline is false, then remove only if it was loaded over two minutes ago.
        this.dataStore.entrySet().removeIf(x -> !x.getValue().getUser().isOnline() && (allOffline || x.getValue().serviceLoadTime().plus(2, ChronoUnit.MINUTES).isBefore(Instant.now())));
    }

    public List<ModularUserService> getOnlineUsersInternal() {
        return ImmutableList.copyOf(dataStore.entrySet().stream().filter(x -> x.getValue().getUser().isOnline()).map(Map.Entry::getValue).collect(Collectors.toList()));
    }

    public void forceUnloadAndDelete(UUID uuid) {
        ModularUserService service = this.dataStore.remove(uuid);
        if (service != null) {
            service.delete();
        }
    }

    public List<ModularUserService> getOnlineUsers() {
        removeOfflinePlayers();
        return ImmutableList.copyOf(dataStore.values());
    }

    public Optional<ModularUserService> getUser(User user) {
        Preconditions.checkNotNull("user");
        return get(user.getUniqueId());
    }
}
