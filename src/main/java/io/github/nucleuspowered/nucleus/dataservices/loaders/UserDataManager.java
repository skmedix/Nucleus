/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices.loaders;

import com.google.common.collect.ImmutableList;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.dataservices.dataproviders.DataProvider;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import ninja.leaping.configurate.ConfigurationNode;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.util.Identifiable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UserDataManager extends DataManager<UUID, ConfigurationNode, ModularUserService> {

    public UserDataManager(BiFunction<UUID, Boolean, DataProvider<ConfigurationNode>> dataProviderFactory, Predicate<UUID> fileExist) {
        super(dataProviderFactory, fileExist);
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
    protected boolean shouldNotExpire(UUID key) {
        return Sponge.getServer().getPlayer(key).isPresent();
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
        this.invalidateOld();
    }

    public void forceUnloadAndDelete(UUID uuid) {
        ModularUserService service = get(uuid).orElse(null);
        this.invalidate(uuid);
        if (service != null) {
            service.delete();
        }
    }

    public List<ModularUserService> getOnlineUsers() {
        return ImmutableList.copyOf(
                getAll(Sponge.getServer().getOnlinePlayers().stream().map(Identifiable::getUniqueId).collect(Collectors.toList())).values()
        );
    }

    public Optional<ModularUserService> getUser(User user) {
        return get(user.getUniqueId());
    }
}
