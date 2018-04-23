/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jail.handlers;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.exceptions.NoSuchLocationException;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Inmate;
import io.github.nucleuspowered.nucleus.api.nucleusdata.NamedLocation;
import io.github.nucleuspowered.nucleus.api.service.NucleusJailService;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularGeneralService;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import io.github.nucleuspowered.nucleus.internal.teleport.NucleusTeleportHandler;
import io.github.nucleuspowered.nucleus.modules.core.datamodules.CoreUserDataModule;
import io.github.nucleuspowered.nucleus.modules.fly.datamodules.FlyUserDataModule;
import io.github.nucleuspowered.nucleus.modules.jail.data.JailData;
import io.github.nucleuspowered.nucleus.modules.jail.datamodules.JailGeneralDataModule;
import io.github.nucleuspowered.nucleus.modules.jail.datamodules.JailUserDataModule;
import io.github.nucleuspowered.nucleus.modules.jail.events.JailEvent;
import io.github.nucleuspowered.nucleus.util.CauseStackHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.context.ContextCalculator;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@NonnullByDefault
public class JailHandler implements NucleusJailService, ContextCalculator<Subject> {

    private final ModularGeneralService store = Nucleus.getNucleus().getGeneralService();

    // Used for the context calculator
    private final Map<UUID, Context> jailDataCache = Maps.newHashMap();
    private final static Context jailContext = new Context(NucleusJailService.JAILED_CONTEXT, "true");

    private JailGeneralDataModule getModule() {
        return this.store.get(JailGeneralDataModule.class);
    }

    @Override
    public Optional<NamedLocation> getJail(String warpName) {
        return getModule().getJailLocation(warpName);
    }

    @Override
    public boolean removeJail(String warpName) {
        return getModule().removeJail(warpName);
    }

    @Override
    public boolean setJail(String warpName, Location<World> location, Vector3d rotation) {
        return getModule().addJail(warpName, location, rotation);
    }

    @Override
    public Map<String, NamedLocation> getJails() {
        return getModule().getJails();
    }

    public boolean isPlayerJailedCached(User user) {
        return this.jailDataCache.containsKey(user.getUniqueId());
    }

    @Override
    public boolean isPlayerJailed(User user) {
        return getPlayerJailDataInternal(user).isPresent();
    }

    @Override
    public Optional<Inmate> getPlayerJailData(User user) {
        return getPlayerJailDataInternal(user).map(x -> x);
    }

    public Optional<JailData> getPlayerJailDataInternal(User user) {
        try {
            Optional<JailData> data = Nucleus.getNucleus().getUserDataManager().get(user, false)
                    .map(y -> y.get(JailUserDataModule.class).getJailData().orElse(null));
            if (data.isPresent()) {
                this.jailDataCache.put(user.getUniqueId(), new Context(NucleusJailService.JAIL_CONTEXT, data.get().getJailName()));
            } else {
                this.jailDataCache.put(user.getUniqueId(), null);
            }

            return data;
        } catch (Exception e) {
            if (Nucleus.getNucleus().isDebugMode()) {
                e.printStackTrace();
            }

            return Optional.empty();
        }
    }

    @Override
    public boolean jailPlayer(User victim, String jail, CommandSource jailer, String reason) throws NoSuchLocationException {
        Preconditions.checkNotNull(victim);
        Preconditions.checkNotNull(jail);
        Preconditions.checkNotNull(jailer);
        Preconditions.checkNotNull(reason);
        NamedLocation location = getJail(jail).orElseThrow(NoSuchLocationException::new);
        return jailPlayer(victim,
                new JailData(Util.getUUID(jailer), location.getName(), reason, victim.getPlayer().map(Locatable::getLocation).orElse(null)));
    }

    public boolean jailPlayer(User user, JailData data) {
        ModularUserService modularUserService = Nucleus.getNucleus().getUserDataManager().getUnchecked(user);
        JailUserDataModule jailUserDataModule = modularUserService.get(JailUserDataModule.class);

        if (jailUserDataModule.getJailData().isPresent()) {
            return false;
        }

        // Get the jail.
        Optional<NamedLocation> owl = getJail(data.getJailName());
        NamedLocation wl = owl.filter(x -> x.getLocation().isPresent()).orElseGet(() -> {
            if (!getJails().isEmpty()) {
                return null;
            }

            return getJails().entrySet().stream().findFirst().get().getValue();
        });

        if (wl == null) {
            return false;
        }

        jailUserDataModule.setJailData(data);
        if (user.isOnline()) {
            Sponge.getScheduler().createSyncExecutor(Nucleus.getNucleus()).execute(() -> {
                Player player = user.getPlayer().get();
                Nucleus.getNucleus().getTeleportHandler().teleportPlayer(player, owl.get().getLocation().get(), owl.get().getRotation(),
                    NucleusTeleportHandler.StandardTeleportMode.NO_CHECK, Sponge.getCauseStackManager().getCurrentCause());
                modularUserService.get(FlyUserDataModule.class).setFlying(false);
            });
        } else {
            jailUserDataModule.setJailOnNextLogin(true);
        }

        this.jailDataCache.put(user.getUniqueId(), new Context(NucleusJailService.JAIL_CONTEXT, data.getJailName()));
            Sponge.getEventManager().post(new JailEvent.Jailed(
            user,
            CauseStackHelper.createCause(Util.getObjectFromUUID(data.getJailerInternal())),
            data.getJailName(),
            TextSerializers.FORMATTING_CODE.deserialize(data.getReason()),
            data.getRemainingTime().orElse(null)));

        return true;
    }

    // Test
    @Override
    public boolean unjailPlayer(User user) {
        return unjailPlayer(user, Sponge.getCauseStackManager().getCurrentCause());
    }

    public boolean unjailPlayer(User user, Cause cause) {
        final ModularUserService modularUserService = Nucleus.getNucleus().getUserDataManager().getUnchecked(user);
        final JailUserDataModule jailUserDataModule = modularUserService.get(JailUserDataModule.class);
        Optional<JailData> ojd = jailUserDataModule.getJailData();
        if (!ojd.isPresent()) {
            return false;
        }

        Optional<Location<World>> ow = ojd.get().getPreviousLocation();
        this.jailDataCache.put(user.getUniqueId(), null);
        if (user.isOnline()) {
            Player player = user.getPlayer().get();
            Sponge.getScheduler().createSyncExecutor(Nucleus.getNucleus()).execute(() -> {
                NucleusTeleportHandler.setLocation(player, ow.orElseGet(() -> player.getWorld().getSpawnLocation()));
                player.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("jail.elapsed"));

                // Remove after the teleport for the back data.
                jailUserDataModule.removeJailData();
            });
        } else {
            modularUserService.get(CoreUserDataModule.class).sendToLocationOnLogin(
                    ow.orElseGet(() -> new Location<>(Sponge.getServer().getWorld(Sponge.getServer().getDefaultWorld().get().getUniqueId()).get(),
                            Sponge.getServer().getDefaultWorld().get().getSpawnPosition())));
            jailUserDataModule.removeJailData();
        }

        Sponge.getEventManager().post(new JailEvent.Unjailed(user, cause));
        return true;
    }

    public Optional<NamedLocation> getWarpLocation(User user) {
        if (!isPlayerJailed(user)) {
            return Optional.empty();
        }

        Optional<NamedLocation> owl = getJail(getPlayerJailDataInternal(user).get().getJailName());
        if (!owl.isPresent()) {
            Collection<NamedLocation> wl = getJails().values();
            if (wl.isEmpty()) {
                return Optional.empty();
            }

            owl = wl.stream().findFirst();
        }

        return owl;
    }

    @Override public void accumulateContexts(Subject calculable, Set<Context> accumulator) {
        if (calculable instanceof User) {
            UUID c = ((User) calculable).getUniqueId();
            if (!this.jailDataCache.containsKey(c)) {
                getPlayerJailDataInternal((User) calculable);
            }

            Context co = this.jailDataCache.get(c);
            if (co != null) {
                accumulator.add(co);
                accumulator.add(jailContext);
            }
        }
    }

    @Override public boolean matches(Context context, Subject subject) {
        if (context.getKey().equals(NucleusJailService.JAIL_CONTEXT)) {
            if (subject instanceof User) {
                UUID u = ((User) subject).getUniqueId();
                return context.equals(this.jailDataCache.get(u));
            }
        } else if (context.getKey().equals(NucleusJailService.JAILED_CONTEXT)) {
            if (subject instanceof User) {
                UUID u = ((User) subject).getUniqueId();
                return this.jailDataCache.get(u) != null;
            }
        }

        return false;
    }

    public boolean checkJail(final User player, boolean sendMessage) {
        Optional<JailData> omd = Util.testForEndTimestamp(getPlayerJailDataInternal(player), () -> unjailPlayer(player));
        if (omd.isPresent()) {
            if (sendMessage) {
                Nucleus.getNucleus().getUserDataManager().getUnchecked(player).get(FlyUserDataModule.class).setFlying(false);
                player.getPlayer().ifPresent(x -> onJail(omd.get(), x));
            }

            return true;
        }

        return false;
    }

    public void onJail(JailData md, Player user) {
        MessageProvider provider = Nucleus.getNucleus().getMessageProvider();
        if (md.getEndTimestamp().isPresent()) {
            user.sendMessage(provider.getTextMessageWithFormat("jail.playernotify.time",
                    Util.getTimeStringFromSeconds(Instant.now().until(md.getEndTimestamp().get(), ChronoUnit.SECONDS))));
        } else {
            user.sendMessage(provider.getTextMessageWithFormat("jail.playernotify.standard"));
        }

        user.sendMessage(provider.getTextMessageWithFormat("standard.reasoncoloured", md.getReason()));
    }
}
