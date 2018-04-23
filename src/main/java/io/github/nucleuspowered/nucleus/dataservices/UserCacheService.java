/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.configurate.datatypes.UserCacheDataNode;
import io.github.nucleuspowered.nucleus.configurate.datatypes.UserCacheVersionNode;
import io.github.nucleuspowered.nucleus.dataservices.dataproviders.DataProvider;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.util.Identifiable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserCacheService extends AbstractService<UserCacheVersionNode> {

    private static final int expectedVersion = new UserCacheVersionNode().getVersion();
    private boolean isWalking = false;

    private final Object lockingObject = new Object();

    public UserCacheService(DataProvider<UserCacheVersionNode> dataProvider) {
        super(dataProvider);
    }

    public List<UUID> getForIp(String ip) {
        updateCacheForOnlinePlayers();
        String ipToCheck = ip.replace("/", "");
        return this.data.getNode().entrySet().stream().filter(x -> x.getValue()
                .getIpAddress().map(y -> y.equals(ipToCheck)).orElse(false))
                .map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public List<UUID> getJailed() {
        updateCacheForOnlinePlayers();
        return this.data.getNode().entrySet().stream().filter(x -> x.getValue().isJailed())
                .map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public List<UUID> getJailedIn(String name) {
        updateCacheForOnlinePlayers();
        return this.data.getNode().entrySet().stream()
                .filter(x -> x.getValue().getJailName().map(y -> y.equalsIgnoreCase(name)).orElse(false))
                .map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public List<UUID> getMuted() {
        updateCacheForOnlinePlayers();
        return this.data.getNode().entrySet().stream().filter(x -> x.getValue().isMuted())
                .map(Map.Entry::getKey).collect(Collectors.toList());
    }

    @Override protected String serviceName() {
        return "User Cache";
    }

    public void updateCacheForOnlinePlayers() {
        Nucleus.getNucleus().getUserDataManager().getOnlineUsers().forEach(u ->
                this.data.getNode().computeIfAbsent(u.getUniqueId(), x -> new UserCacheDataNode()).set(u));
    }

    public void updateCacheForPlayer(ModularUserService u) {
        this.data.getNode().computeIfAbsent(u.getUniqueId(), x -> new UserCacheDataNode()).set(u);
    }

    public void updateCacheForPlayer(UUID uuid) {
        Nucleus.getNucleus().getUserDataManager().get(uuid).ifPresent(this::updateCacheForPlayer);
    }

    public void startFilewalkIfNeeded() {
        if (!this.isWalking && (!isCorrectVersion() || this.data.getNode().isEmpty())) {
            fileWalk();
        }
    }

    public boolean isCorrectVersion() {
        return expectedVersion == this.data.getVersion();
    }

    public boolean fileWalk() {
        synchronized (this.lockingObject) {
            if (this.isWalking) {
                return false;
            }

            this.isWalking = true;
        }

        try {
            Map<UUID, UserCacheDataNode> data = Maps.newHashMap();
            List<UUID> knownUsers = Sponge.getServiceManager().provideUnchecked(UserStorageService.class).getAll().stream()
                    .map(Identifiable::getUniqueId).collect(Collectors.toList());

            int count = 0;
            UserDataManager manager = Nucleus.getNucleus().getUserDataManager();
            for (UUID user : knownUsers) {
                if (manager.has(user)) {
                    manager.get(user).ifPresent(x -> data.put(user, new UserCacheDataNode(x)));
                    if (++count >= 10) {
                        manager.removeOfflinePlayers();
                        count = 0;
                    }
                }
            }

            this.data = new UserCacheVersionNode();
            this.data.getNode().putAll(data);
            save();
        } finally {
            this.isWalking = false;
        }

        return true;
    }
}
