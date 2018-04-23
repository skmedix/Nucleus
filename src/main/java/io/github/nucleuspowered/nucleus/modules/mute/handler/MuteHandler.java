/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mute.handler;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.nucleusdata.MuteInfo;
import io.github.nucleuspowered.nucleus.api.service.NucleusMuteService;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import io.github.nucleuspowered.nucleus.modules.mute.data.MuteData;
import io.github.nucleuspowered.nucleus.modules.mute.datamodules.MuteUserDataModule;
import io.github.nucleuspowered.nucleus.modules.mute.events.MuteEvent;
import io.github.nucleuspowered.nucleus.util.CauseStackHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.context.ContextCalculator;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Identifiable;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

public class MuteHandler implements ContextCalculator<Subject>, NucleusMuteService {

    private final UserDataManager ucl;

    private final Map<UUID, Boolean> muteContextCache = Maps.newHashMap();
    private final Context mutedContext = new Context(NucleusMuteService.MUTED_CONTEXT, "true");

    private boolean globalMuteEnabled = false;
    private final List<UUID> voicedUsers = Lists.newArrayList();

    public MuteHandler() {
        this.ucl = Nucleus.getNucleus().getUserDataManager();
    }

    public void onMute(MuteData md, Player user) {
        MessageProvider messageProvider = Nucleus.getNucleus().getMessageProvider();
        if (md.getEndTimestamp().isPresent()) {
            user.sendMessage(messageProvider.getTextMessageWithFormat("mute.playernotify.time",
                    Util.getTimeStringFromSeconds(Instant.now().until(md.getEndTimestamp().get(), ChronoUnit.SECONDS))));
        } else {
            user.sendMessage(messageProvider.getTextMessageWithFormat("mute.playernotify.standard"));
        }
    }

    @Override public boolean isMuted(User user) {
        return getPlayerMuteData(user).isPresent();
    }

    @Override public Optional<MuteInfo> getPlayerMuteInfo(User user) {
        return getPlayerMuteData(user).map(x -> x);
    }

    // Internal
    public Optional<MuteData> getPlayerMuteData(User user) {
        Optional<MuteData> nu = this.ucl.get(user, false).map(x -> x.get(MuteUserDataModule.class).getMuteData().orElse(null));
        this.muteContextCache.put(user.getUniqueId(), nu.isPresent());
        return nu;
    }

    @Override public boolean mutePlayer(User user, String reason, @Nullable Duration duration, Cause cause) {
        UUID first = cause.first(User.class).map(Identifiable::getUniqueId).orElse(Util.consoleFakeUUID);
        return mutePlayer(user, new MuteData(first, reason, duration), cause);
    }

    public boolean mutePlayer(User user, MuteData data) {
        return mutePlayer(user, data, CauseStackHelper.createCause((Util.getObjectFromUUID(data.getMuterInternal()))));
    }

    public boolean mutePlayer(User user, MuteData data, Cause cause) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(data);

        Optional<ModularUserService> nu = this.ucl.get(user);
        if (!nu.isPresent()) {
            return false;
        }

        Instant time = Instant.now();
        ModularUserService u = nu.get();
        final Duration d = data.getRemainingTime().orElse(null);
        if (user.isOnline() && data.getTimeFromNextLogin().isPresent() && !data.getEndTimestamp().isPresent()) {
            data.setEndtimestamp(time.plus(data.getTimeFromNextLogin().get()));
        }

        u.get(MuteUserDataModule.class).setMuteData(data);
        this.muteContextCache.put(u.getUniqueId(), true);
        Sponge.getEventManager().post(new MuteEvent.Muted(
                cause,
                user,
                d,
                Text.of(data.getReason())));
        return true;
    }

    public boolean unmutePlayer(User user) {
        return unmutePlayer(user, CauseStackHelper.createCause(), true);
    }

    @Override public boolean unmutePlayer(User user, Cause cause) {
        return unmutePlayer(user, cause, false);
    }

    public boolean unmutePlayer(User user, Cause cause, boolean expired) {
        if (isMuted(user)) {
            Optional<ModularUserService> o = this.ucl.get(user);
            if (o.isPresent()) {
                o.get().get(MuteUserDataModule.class).removeMuteData();
                this.muteContextCache.put(user.getUniqueId(), false);
                Sponge.getEventManager().post(new MuteEvent.Unmuted(
                        cause,
                        user,
                        expired));

                user.getPlayer().ifPresent(x ->
                    x.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("mute.elapsed")));
                return true;
            }
        }

        return false;
    }

    public boolean isGlobalMuteEnabled() {
        return this.globalMuteEnabled;
    }

    public void setGlobalMuteEnabled(boolean globalMuteEnabled) {
        if (this.globalMuteEnabled != globalMuteEnabled) {
            this.voicedUsers.clear();
        }

        this.globalMuteEnabled = globalMuteEnabled;
    }

    public boolean isVoiced(UUID uuid) {
        return this.voicedUsers.contains(uuid);
    }

    public void addVoice(UUID uuid) {
        this.voicedUsers.add(uuid);
    }

    public void removeVoice(UUID uuid) {
        this.voicedUsers.remove(uuid);
    }

    @Override public void accumulateContexts(Subject calculable, Set<Context> accumulator) {
        if (calculable instanceof User) {
            UUID u = ((User) calculable).getUniqueId();
            if (this.muteContextCache.computeIfAbsent(u, k -> isMuted((User) calculable))) {
                accumulator.add(this.mutedContext);
            }
        }
    }

    @Override public boolean matches(Context context, Subject subject) {
        return context.getKey().equals(NucleusMuteService.MUTED_CONTEXT) && subject instanceof User &&
                this.muteContextCache.computeIfAbsent(((User) subject).getUniqueId(), k -> isMuted((User) subject));
    }

    public boolean isMutedCached(User x) {
        return this.muteContextCache.containsKey(x.getUniqueId());
    }
}
