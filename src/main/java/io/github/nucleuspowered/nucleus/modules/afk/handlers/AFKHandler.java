/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk.handlers;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.service.NucleusAFKService;
import io.github.nucleuspowered.nucleus.api.util.NoExceptionAutoClosable;
import io.github.nucleuspowered.nucleus.internal.CommandPermissionHandler;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.permissions.ServiceChangeListener;
import io.github.nucleuspowered.nucleus.internal.text.NucleusTextTemplateImpl;
import io.github.nucleuspowered.nucleus.modules.afk.commands.AFKCommand;
import io.github.nucleuspowered.nucleus.modules.afk.config.AFKConfig;
import io.github.nucleuspowered.nucleus.modules.afk.config.AFKConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.afk.events.AFKEvents;
import io.github.nucleuspowered.nucleus.util.CauseStackHelper;
import io.github.nucleuspowered.nucleus.util.Tuples;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.chat.ChatTypes;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.concurrent.GuardedBy;

public class AFKHandler implements NucleusAFKService, Reloadable {

    private final Map<UUID, AFKData> data = Maps.newConcurrentMap();
    private final AFKConfigAdapter afkConfigAdapter;
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

    public AFKHandler() {
        this.afkPermissionHandler = Nucleus.getNucleus().getPermissionRegistry().getPermissionsForNucleusCommand(AFKCommand.class);
        this.afkConfigAdapter = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(AFKConfigAdapter.class);
    }

    public void stageUserActivityUpdate(Player player) {
        if (player.isOnline()) {
            stageUserActivityUpdate(player.getUniqueId());
        }
    }

    private void stageUserActivityUpdate(UUID uuid) {
        synchronized (this.lock) {
            synchronized (this.lock2) {
                if (this.disabledTracking.containsKey(uuid)) {
                    return;
                }
            }

            this.activity.add(uuid);
        }
    }

    public void onTick() {
        synchronized (this.lock) {
            this.activity.forEach(u -> this.data.compute(u, ((uuid, afkData) -> afkData == null ? new AFKData(uuid) : updateActivity(uuid, afkData))));
            this.activity.clear();
        }

        List<UUID> uuidList = Sponge.getServer().getOnlinePlayers().stream().map(Player::getUniqueId).collect(Collectors.toList());

        // Remove all offline players.
        Set<Map.Entry<UUID, AFKData>> entries = this.data.entrySet();
        entries.removeIf(refactor -> !uuidList.contains(refactor.getKey()));
        entries.stream().filter(x -> !x.getValue().cacheValid).forEach(x -> x.getValue().updateFromPermissions());

        long now = System.currentTimeMillis();

        // Check AFK status.
        entries.stream().filter(x -> x.getValue().isKnownAfk && !x.getValue().willKick && x.getValue().timeToKick > 0).forEach(e -> {
            if (now - e.getValue().lastActivityTime > e.getValue().timeToKick) {
                // Kick them
                e.getValue().willKick = true;
                NucleusTextTemplateImpl message = this.config.getMessages().getKickMessage();
                TextRepresentable t;
                if (message == null || message.isEmpty()) {
                    t = Nucleus.getNucleus().getMessageProvider().getTextMessageWithTextFormat("afk.kickreason");
                } else {
                    t = message;
                }

                final NucleusTextTemplateImpl messageToServer = this.config.getMessages().getOnKick();

                Sponge.getServer().getPlayer(e.getKey()).ifPresent(player -> {
                    MessageChannel mc;
                    if (this.config.isBroadcastOnKick()) {
                        mc = MessageChannel.TO_ALL;
                    } else {
                        mc = MessageChannel.permission(this.afkPermissionHandler.getPermissionWithSuffix("notify"));
                    }

                    AFKEvents.Kick events = new AFKEvents.Kick(player, messageToServer.getForCommandSource(player), mc);
                    if (Sponge.getEventManager().post(events)) {
                        // Cancelled.
                        return;
                    }

                    Text toSend = t instanceof NucleusTextTemplateImpl ? ((NucleusTextTemplateImpl) t).getForCommandSource(player) : t.toText();
                    Sponge.getScheduler().createSyncExecutor(Nucleus.getNucleus()).execute(() -> player.kick(toSend));
                    events.getMessage().ifPresent(m -> events.getChannel().send(player, m, ChatTypes.SYSTEM));
                });
            }
        });

        // Check AFK status.
        entries.stream().filter(x -> !x.getValue().isKnownAfk && x.getValue().timeToAfk > 0).forEach(e -> {
            if (now - e.getValue().lastActivityTime  > e.getValue().timeToAfk) {
                Sponge.getServer().getPlayer(e.getKey()).ifPresent(this::setAfkInternal);
            }
        });
    }

    public void invalidateAfkCache() {
        this.data.forEach((k, v) -> v.cacheValid = false);
    }

    public boolean isAFK(UUID uuid) {
        return this.data.containsKey(uuid) && this.data.get(uuid).isKnownAfk;
    }

    public boolean setAfkInternal(Player player) {
        return setAfkInternal(player, CauseStackHelper.createCause(player), false);
    }

    public boolean setAfkInternal(Player player, Cause cause, boolean force) {
        if (!player.isOnline()) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        AFKData a = this.data.compute(uuid, ((u, afkData) -> afkData == null ? new AFKData(u) : afkData));
        if (force) {
            a.isKnownAfk = false;
        } else if (a.isKnownAfk) {
            return false;
        }

        if (a.canGoAfk()) {
            // Don't accident undo setting AFK, remove any activity from the list.
            synchronized (this.lock) {
                this.activity.remove(uuid);
            }

            Tuples.NullableTuple<Text, MessageChannel> ttmc = getAFKMessage(player, true);
            AFKEvents.To event = new AFKEvents.To(player, ttmc.getFirstUnwrapped(), ttmc.getSecondUnwrapped(), cause);
            Sponge.getEventManager().post(event);
            actionEvent(event, "command.afk.to.vanish");

            a.isKnownAfk = true;
            return true;
        }

        return false;
    }

    @Override
    public void onReload() {
        this.config = this.afkConfigAdapter.getNodeOrDefault();
    }

    private AFKData updateActivity(UUID uuid, AFKData data) {
        List<Object> lo = Lists.newArrayList();
        Sponge.getServer().getPlayer(uuid).ifPresent(lo::add);
        return updateActivity(uuid, data, CauseStackHelper.createCause(lo));
    }

    private AFKData updateActivity(UUID uuid, AFKData data, Cause cause) {
        data.lastActivityTime = System.currentTimeMillis();
        if (data.isKnownAfk) {
            data.isKnownAfk = false;
            data.willKick = false;
            Sponge.getServer().getPlayer(uuid).ifPresent(x -> {
                Tuples.NullableTuple<Text, MessageChannel> ttmc = getAFKMessage(x, false);
                AFKEvents.From event = new AFKEvents.From(x, ttmc.getFirstUnwrapped(), ttmc.getSecondUnwrapped(), cause);
                Sponge.getEventManager().post(event);
                actionEvent(event, "command.afk.from.vanish");
            });

        }

        return data;
    }

    private void actionEvent(AFKEvents event, String key) {
        Optional<Text> message = event.getMessage();
        if (message.isPresent()) {
            if (!message.get().isEmpty()) {
                event.getChannel().send(event.getTargetEntity(), event.getMessage().get(), ChatTypes.SYSTEM);
            }
        } else {
            event.getTargetEntity().sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat(key));
        }
    }

    private Tuples.NullableTuple<Text, MessageChannel> getAFKMessage(Player player, boolean isAfk) {
        if (this.config.isAfkOnVanish() || !player.get(Keys.VANISH).orElse(false)) {
            NucleusTextTemplateImpl template = isAfk ? this.config.getMessages().getAfkMessage() : this.config.getMessages().getReturnAfkMessage();
            return Tuples.ofNullable(template.getForCommandSource(player), MessageChannel.TO_ALL);
        } else {
            return Tuples.ofNullable(null, MessageChannel.TO_NONE);
        }
    }

    @Override public boolean canGoAFK(User user) {
        return getData(user.getUniqueId()).canGoAfk();
    }

    @Override public boolean isAFK(Player player) {
        return isAFK(player.getUniqueId());
    }

    @Override public boolean setAFK(Cause cause, Player player, boolean isAfk) {
        Preconditions.checkArgument(cause.root() instanceof PluginContainer, "The root object MUST be a plugin container.");
        AFKData data = this.data.computeIfAbsent(player.getUniqueId(), AFKData::new);
        if (data.isKnownAfk == isAfk) {
            // Already AFK
            return false;
        }

        if (isAfk) {
            return setAfkInternal(player, cause, false);
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

    @Override public NoExceptionAutoClosable disableTrackingForPlayer(final Player player, int ticks) {
        // Disable tracking now with a new UUID.
        Task n = Task.builder().execute(t -> {
            synchronized (this.lock2) {
                this.disabledTracking.remove(player.getUniqueId(), t.getUniqueId());
            }
        }).delayTicks(ticks).submit(Nucleus.getNucleus());

        synchronized (this.lock2) {
            this.disabledTracking.put(player.getUniqueId(), n.getUniqueId());
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

    @Override
    public Collection<Player> getAfk() {
        return getAfk(x -> true);
    }

    public Collection<Player> getAfk(Predicate<Player> filter) {
        return this.data.entrySet().stream()
                .filter(x -> x.getValue().isKnownAfk)
                .map(x -> Sponge.getServer().getPlayer(x.getKey()).orElse(null))
                .filter(Objects::nonNull)
                .filter(filter)
                .collect(Collectors.toList());
    }

    class AFKData {

        private final UUID uuid;

        private long lastActivityTime = System.currentTimeMillis();
        boolean isKnownAfk = false;
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
            this.cacheValid = false;
            updateFromPermissions();
            return this.timeToAfk > 0;
        }

        private boolean canBeKicked() {
            this.cacheValid = false;
            updateFromPermissions();
            return this.timeToKick > 0;
        }

        void updateFromPermissions() {
            synchronized (this) {
                if (!this.cacheValid) {
                    // Get the subject.
                    Sponge.getServer().getPlayer(this.uuid).ifPresent(x -> {
                        if (!ServiceChangeListener.isOpOnly() && AFKHandler.this.afkPermissionHandler.testSuffix(x, AFKHandler.this.exempttoggle)) {
                            this.timeToAfk = -1;
                        } else {
                            this.timeToAfk = Util.getPositiveLongOptionFromSubject(x,
                                    AFKHandler.this.afkOption).orElseGet(() -> AFKHandler.this.config.getAfkTime()) * 1000;
                        }

                        if (AFKHandler.this.afkPermissionHandler.testSuffix(x, AFKHandler.this.exemptkick)) {
                            this.timeToKick = -1;
                        } else {
                            this.timeToKick = Util.getPositiveLongOptionFromSubject(x,
                                    AFKHandler.this.afkKickOption).orElseGet(() -> AFKHandler.this.config.getAfkTimeToKick()) * 1000;
                        }

                        this.cacheValid = true;
                    });
                }
            }
        }
    }
}
