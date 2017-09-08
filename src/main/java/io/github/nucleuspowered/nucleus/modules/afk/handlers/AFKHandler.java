/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk.handlers;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.service.NucleusAFKService;
import io.github.nucleuspowered.nucleus.internal.CommandPermissionHandler;
import io.github.nucleuspowered.nucleus.internal.text.NucleusTextTemplateImpl;
import io.github.nucleuspowered.nucleus.modules.afk.commands.AFKCommand;
import io.github.nucleuspowered.nucleus.modules.afk.config.AFKConfig;
import io.github.nucleuspowered.nucleus.modules.afk.config.AFKConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.afk.events.AFKEvents;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.text.channel.MessageChannel;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.concurrent.GuardedBy;

public class AFKHandler implements NucleusAFKService {

    private final Map<UUID, AFKData> data = Maps.newConcurrentMap();
    private final AFKConfigAdapter afkConfigAdapter;
    private final NucleusPlugin plugin;
    private final CommandPermissionHandler afkPermissionHandler;
    private AFKConfig config;

    @GuardedBy("lock")
    private final Set<UUID> activity = Sets.newHashSet();

    @GuardedBy("lock2")
    private final Multimap<UUID, UUID> disabledTracking = HashMultimap.create();
    private final Object lock = new Object();
    private final Object lock2 = new Object();

    private final String exempttoggle = "exempt.toggle";
    private final String exemptkick = "exempt.kick";

    private final String afkOption = "nucleus.afk.toggletime";
    private final String afkKickOption = "nucleus.afk.kicktime";

    public AFKHandler(NucleusPlugin plugin, AFKConfigAdapter afkConfigAdapter) {
        this.plugin = plugin;
        this.afkPermissionHandler = plugin.getPermissionRegistry().getPermissionsForNucleusCommand(AFKCommand.class);
        this.afkConfigAdapter = afkConfigAdapter;
        plugin.registerReloadable(this::onReload);
        onReload();
    }

    public void stageUserActivityUpdate(Player player) {
        if (player.isOnline()) {
            stageUserActivityUpdate(player.getUniqueId());
        }
    }

    private void stageUserActivityUpdate(UUID uuid) {
        synchronized (lock) {
            synchronized (lock2) {
                if (this.disabledTracking.containsKey(uuid)) {
                    return;
                }
            }

            activity.add(uuid);
        }
    }

    public void onTick() {
        synchronized (lock) {
            activity.forEach(u -> data.compute(u, ((uuid, afkData) -> afkData == null ? new AFKData(uuid) : updateActivity(uuid, afkData))));
            activity.clear();
        }

        List<UUID> uuidList = Sponge.getServer().getOnlinePlayers().stream().map(Player::getUniqueId).collect(Collectors.toList());

        // Remove all offline players.
        Set<Map.Entry<UUID, AFKData>> entries = data.entrySet();
        entries.removeIf(refactor -> !uuidList.contains(refactor.getKey()));
        entries.stream().filter(x -> !x.getValue().cacheValid).forEach(x -> x.getValue().updateFromPermissions());

        long now = System.currentTimeMillis();

        // Check AFK status.
        entries.stream().filter(x -> x.getValue().isKnownAfk && !x.getValue().willKick && x.getValue().timeToKick > 0).forEach(e -> {
            if (now - e.getValue().lastActivityTime > e.getValue().timeToKick) {
                // Kick them
                e.getValue().willKick = true;
                NucleusTextTemplateImpl message = config.getMessages().getKickMessage();
                TextRepresentable t;
                if (message.isEmpty()) {
                    t = plugin.getMessageProvider().getTextMessageWithTextFormat("afk.kickreason");
                } else {
                    t = message;
                }

                final NucleusTextTemplateImpl messageToServer = config.getMessages().getOnKick();

                Sponge.getServer().getPlayer(e.getKey()).ifPresent(player -> {
                    if (Sponge.getEventManager().post(new AFKEvents.Kick(player, Cause.of(NamedCause.owner(plugin))))) {
                        // Cancelled.
                        return;
                    }

                    Text toSend = t instanceof NucleusTextTemplateImpl ? ((NucleusTextTemplateImpl) t).getForCommandSource(player) : t.toText();
                    Sponge.getScheduler().createSyncExecutor(plugin)
                        .execute(() -> player.kick(toSend));
                    if (!messageToServer.isEmpty()) {
                        MessageChannel mc;
                        if (config.isBroadcastOnKick()) {
                            mc = MessageChannel.TO_ALL;
                        } else {
                            mc = MessageChannel.permission(this.afkPermissionHandler.getPermissionWithSuffix("notify"));
                        }

                        mc.send(messageToServer.getForCommandSource(player));
                    }
                });
            }
        });

        // Check AFK status.
        entries.stream().filter(x -> !x.getValue().isKnownAfk && x.getValue().timeToAfk > 0).forEach(e -> {
            if (now - e.getValue().lastActivityTime  > e.getValue().timeToAfk) {
                Sponge.getServer().getPlayer(e.getKey()).ifPresent(this::setAfk);
            }
        });
    }

    public void invalidateAfkCache() {
        data.forEach((k, v) -> v.cacheValid = false);
    }

    public boolean isAfk(Player player) {
        return isAfk(player.getUniqueId());
    }

    public boolean isAfk(UUID uuid) {
        return data.containsKey(uuid) && data.get(uuid).isKnownAfk;
    }

    public boolean setAfk(Player player) {
        return setAfk(player, Cause.of(NamedCause.owner(player)), false);
    }

    public boolean setAfk(Player player, Cause cause, boolean force) {
        if (!player.isOnline()) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        AFKData a = data.compute(uuid, ((u, afkData) -> afkData == null ? new AFKData(u) : afkData));
        if (force) {
            a.isKnownAfk = false;
        } else if (a.isKnownAfk) {
            return false;
        }

        if (a.canGoAfk()) {
            // Don't accident undo setting AFK, remove any activity from the list.
            synchronized (lock) {
                activity.remove(uuid);
            }

            Sponge.getEventManager().post(new AFKEvents.To(player, cause));

            a.isKnownAfk = true;
            sendAFKMessage(uuid, true);
            return true;
        }

        return false;
    }

    private void onReload() {
        config = afkConfigAdapter.getNodeOrDefault();
    }

    private AFKData updateActivity(UUID uuid, AFKData data) {
        return updateActivity(uuid, data, Cause.of(NamedCause.source(Sponge.getServer().getPlayer(uuid).map(x -> (Object)x).orElse(plugin))));
    }

    private AFKData updateActivity(UUID uuid, AFKData data, Cause cause) {
        data.lastActivityTime = System.currentTimeMillis();
        if (data.isKnownAfk) {
            data.isKnownAfk = false;
            data.willKick = false;
            Sponge.getServer().getPlayer(uuid).ifPresent(x -> Sponge.getEventManager().post(new AFKEvents.From(x, cause)));

            // Send message.
            sendAFKMessage(uuid, false);
        }

        return data;
    }

    private void sendAFKMessage(UUID uuid, boolean isAfk) {
        Sponge.getServer().getPlayer(uuid).ifPresent(player -> {
            // If we have the config set to true, or the subject is NOT invisible, send an AFK message
            if (config.isAfkOnVanish() || !player.get(Keys.VANISH).orElse(false)) {
                NucleusTextTemplateImpl template = isAfk ? config.getMessages().getAfkMessage() : config.getMessages().getReturnAfkMessage();
                if (!template.isEmpty()) {
                    MessageChannel.TO_ALL.send(template.getForCommandSource(player));
                }
            } else {
                // Tell the user in question about them going AFK
                player.sendMessage(
                    NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat(isAfk ? "command.afk.to.vanish" : "command.afk.from.vanish"));
            }
        });
    }

    @Override public boolean canGoAFK(User user) {
        return getData(user.getUniqueId()).canGoAfk();
    }

    @Override public boolean isAFK(Player player) {
        return this.data.computeIfAbsent(player.getUniqueId(), AFKData::new).isKnownAfk;
    }

    @Override public boolean setAFK(Cause cause, Player player, boolean isAfk) {
        Preconditions.checkArgument(cause.root() instanceof PluginContainer, "The root object MUST be a plugin container.");
        AFKData data = this.data.computeIfAbsent(player.getUniqueId(), AFKData::new);
        if (data.isKnownAfk == isAfk) {
            // Already AFK
            return false;
        }

        if (isAfk) {
            return setAfk(player, cause, false);
        } else {
            return !updateActivity(player.getUniqueId(), data, cause).isKnownAfk;
        }
    }

    @Override public boolean canBeKicked(User user) {
        return getData(user.getUniqueId()).canBeKicked();
    }

    @Override public Instant lastActivity(Player player) {
        return Instant.ofEpochMilli(this.data.computeIfAbsent(player.getUniqueId(), AFKData::new).lastActivityTime);
    }

    @Override public Optional<Duration> timeForInactivity(User user) {
        AFKData data = getData(user.getUniqueId());
        if (data.canGoAfk()) {
            return Optional.of(Duration.ofMillis(data.timeToAfk));
        }

        return Optional.empty();
    }

    @Override public Optional<Duration> timeForKick(User user) {
        AFKData data = getData(user.getUniqueId());
        if (data.canBeKicked()) {
            return Optional.of(Duration.ofMillis(data.timeToKick));
        }

        return Optional.empty();
    }

    @Override public void invalidateCachedPermissions() {
        invalidateAfkCache();
    }

    @Override public void updateActivityForUser(Player player) {
        stageUserActivityUpdate(player);
    }

    @Override public AutoCloseable disableTrackingFor(final Player player, int ticks) {
        // Disable tracking now with a new UUID.
        Task n = Task.builder().execute(t -> {
            synchronized (lock2) {
                disabledTracking.remove(player.getUniqueId(), t.getUniqueId());
            }
        }).delayTicks(ticks).submit(plugin);

        synchronized (lock2) {
            disabledTracking.put(player.getUniqueId(), n.getUniqueId());
        }

        return () -> {
            n.cancel();
            n.getConsumer().accept(n);
        };
    }

    private AFKData getData(UUID uuid) {
        AFKData data = this.data.get(uuid);
        if (data == null) {
            // Prevent more checks
            data = new AFKData(uuid, false);
        }

        return data;
    }

    class AFKData {

        private final UUID uuid;

        private long lastActivityTime = System.currentTimeMillis();
        private boolean isKnownAfk = false;
        private boolean willKick = false;

        private boolean cacheValid = false;
        private long timeToAfk = -1;
        private long timeToKick = -1;

        private AFKData(UUID uuid) {
            this(uuid, true);
        }

        private AFKData(UUID uuid, boolean permCheck) {
            this.uuid = uuid;
            if (permCheck) {
                updateFromPermissions();
            }
        }

        private boolean canGoAfk() {
            cacheValid = false;
            updateFromPermissions();
            return timeToAfk > 0;
        }

        private boolean canBeKicked() {
            cacheValid = false;
            updateFromPermissions();
            return timeToKick > 0;
        }

        void updateFromPermissions() {
            synchronized (this) {
                if (!cacheValid) {
                    // Get the subject.
                    Sponge.getServer().getPlayer(uuid).ifPresent(x -> {
                        if (AFKHandler.this.afkPermissionHandler.testSuffix(x, exempttoggle)) {
                            timeToAfk = -1;
                        } else {
                            timeToAfk = Util.getPositiveLongOptionFromSubject(x, afkOption).orElseGet(() -> config.getAfkTime()) * 1000;
                        }

                        if (AFKHandler.this.afkPermissionHandler.testSuffix(x, exemptkick)) {
                            timeToKick = -1;
                        } else {
                            timeToKick = Util.getPositiveLongOptionFromSubject(x, afkKickOption).orElseGet(() -> config.getAfkTimeToKick()) * 1000;
                        }

                        cacheValid = true;
                    });
                }
            }
        }
    }
}
