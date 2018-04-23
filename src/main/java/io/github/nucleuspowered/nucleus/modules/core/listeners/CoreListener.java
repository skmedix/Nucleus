/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.listeners;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.events.NucleusFirstJoinEvent;
import io.github.nucleuspowered.nucleus.api.text.NucleusTextTemplate;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import io.github.nucleuspowered.nucleus.internal.permissions.ServiceChangeListener;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfig;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.core.datamodules.CoreUserDataModule;
import io.github.nucleuspowered.nucleus.modules.core.datamodules.UniqueUserCountTransientModule;
import io.github.nucleuspowered.nucleus.modules.core.events.NucleusOnLoginEvent;
import io.github.nucleuspowered.nucleus.modules.core.events.OnFirstLoginEvent;
import io.github.nucleuspowered.nucleus.util.CauseStackHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

public class CoreListener implements Reloadable, ListenerBase {

    @Nullable private NucleusTextTemplate getKickOnStopMessage = null;
    @Nullable private final URL url;
    private boolean warnOnWildcard = true;

    public CoreListener() {
        URL u = null;
        try {
            u = new URL("https://ore.spongepowered.org/Nucleus/Nucleus/pages/The-Permissions-Wildcard-(And-Why-You-Shouldn't-Use-It)");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.url = u;
    }

    @IsCancelled(Tristate.UNDEFINED)
    @Listener(order = Order.FIRST)
    public void onPlayerLoginFirst(final ClientConnectionEvent.Login event, @Getter("getTargetUser") User user) {
        // This works here. Not complaining.
        if (Util.isFirstPlay(user)) {
            Nucleus.getNucleus().getUserDataManager().get(user).ifPresent(qsu -> {
                CoreUserDataModule cu = qsu.get(CoreUserDataModule.class);
                if (!cu.getLastLogout().isPresent()) {
                    cu.setStartedFirstJoin(true);
                }
            });
        }
    }

    /* (non-Javadoc)
     * We do this last to avoid interfering with other modules.
     */
    @Listener(order = Order.LATE)
    public void onPlayerLoginLast(final ClientConnectionEvent.Login event, @Getter("getProfile") GameProfile profile,
        @Getter("getTargetUser") User user) {

        Nucleus.getNucleus().getUserDataManager().get(profile.getUniqueId()).ifPresent(qsu -> {
            if (event.getFromTransform().equals(event.getToTransform())) {
                CoreUserDataModule c = qsu.get(CoreUserDataModule.class);
                // Check this
                NucleusOnLoginEvent onLoginEvent =
                        CauseStackHelper.createFrameWithCausesWithReturn(cause ->
                                new NucleusOnLoginEvent(cause, user, qsu, event.getFromTransform()), profile);

                Sponge.getEventManager().post(onLoginEvent);
                if (onLoginEvent.getTo().isPresent()) {
                    event.setToTransform(onLoginEvent.getTo().get());
                    c.removeLocationOnLogin();
                    return;
                }

                // If we have a location to send them to in the config, send them there now!
                Optional<Location<World>> olw = c.getLocationOnLogin();
                olw.ifPresent(worldLocation -> {
                    event.setToTransform(event.getFromTransform().setLocation(worldLocation));
                    c.removeLocationOnLogin();
                });
            }

            Nucleus.getNucleus().getUserCacheService().updateCacheForPlayer(qsu);
        });
    }

    /* (non-Javadoc)
     * We do this first to try to get the first play status as quick as possible.
     */
    @Listener(order = Order.FIRST)
    public void onPlayerJoinFirst(final ClientConnectionEvent.Join event, @Getter("getTargetEntity") final Player player) {
        try {
            ModularUserService qsu = Nucleus.getNucleus().getUserDataManager().getUnchecked(player);
            CoreUserDataModule c = qsu.get(CoreUserDataModule.class);
            c.setLastLogin(Instant.now());

            // If in the cache, unset it too.
            c.setFirstPlay(c.isStartedFirstJoin() && !c.getLastLogout().isPresent());

            if (c.isFirstPlay()) {
                Nucleus.getNucleus().getGeneralService().getTransient(UniqueUserCountTransientModule.class).resetUniqueUserCount();
            }

            c.setFirstJoin(player.getJoinData().firstPlayed().get());
            if (Nucleus.getNucleus().isServer()) {
                c.setLastIp(player.getConnection().getAddress().getAddress());
            }

            // We'll do this bit shortly - after the login events have resolved.
            final String name = player.getName();
            Task.builder().execute(() -> c.setLastKnownName(name)).delayTicks(20L).submit(Nucleus.getNucleus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Listener
    public void onPlayerJoinLast(final ClientConnectionEvent.Join event, @Getter("getTargetEntity") final Player player) {
        if (Nucleus.getNucleus().getUserDataManager().get(player).map(x -> x.get(CoreUserDataModule.class).isFirstPlay()).orElse(true)) {
            NucleusFirstJoinEvent firstJoinEvent = new OnFirstLoginEvent(
                event.getCause(), player, event.getOriginalChannel(), event.getChannel().orElse(null), event.getOriginalMessage(),
                    event.isMessageCancelled(), event.getFormatter());

            Sponge.getEventManager().post(firstJoinEvent);
            event.setChannel(firstJoinEvent.getChannel().get());
            event.setMessageCancelled(firstJoinEvent.isMessageCancelled());
            Nucleus.getNucleus().getUserDataManager().getUnchecked(player).get(CoreUserDataModule.class).setStartedFirstJoin(false);
        }

        // Warn about wildcard.
        if (!ServiceChangeListener.isOpOnly() && player.hasPermission("nucleus")) {
            MessageProvider provider = Nucleus.getNucleus().getMessageProvider();
            Nucleus.getNucleus().getLogger().warn("The player " + player.getName() + " has got either the nucleus wildcard or the * wildcard "
                    + "permission. This may cause unintended side effects.");

            if (this.warnOnWildcard) {
                // warn
                List<Text> text = Lists.newArrayList();
                text.add(provider.getTextMessageWithFormat("core.permission.wildcard2"));
                text.add(provider.getTextMessageWithFormat("core.permission.wildcard3"));
                if (this.url != null) {
                    text.add(
                            provider.getTextMessageWithFormat("core.permission.wildcard4").toBuilder()
                                    .onClick(TextActions.openUrl(this.url)).build()
                    );
                }
                text.add(provider.getTextMessageWithFormat("core.permission.wildcard5"));
                Sponge.getServiceManager().provideUnchecked(PaginationService.class)
                        .builder()
                        .title(provider.getTextMessageWithFormat("core.permission.wildcard"))
                        .contents(text)
                        .padding(Text.of(TextColors.GOLD, "-"))
                        .sendTo(player);
            }
        }
    }

    @Listener(beforeModifications = true)
    @SuppressWarnings("ConstantConditionalExpression")
    public void onPlayerQuit(final ClientConnectionEvent.Disconnect event, @Getter("getTargetEntity") final Player player) {
        // There is an issue in Sponge where the connection may not even exist, because they were disconnected before the connection was
        // completely established.
        //noinspection ConstantConditions
        if (player.getConnection() == null || player.getConnection().getAddress() == null) {
            return;
        }

        Nucleus.getNucleus().getUserDataManager().get(player).ifPresent(x -> onPlayerQuit(x, player));
    }

    private void onPlayerQuit(ModularUserService x, Player player) {
        final Location<World> location = player.getLocation();
        final InetAddress address = player.getConnection().getAddress().getAddress();

        try {
            CoreUserDataModule coreUserDataModule = x.get(CoreUserDataModule.class);
            coreUserDataModule.setLastIp(address);
            coreUserDataModule.setLastLogout(location);
            x.save();
            Nucleus.getNucleus().getUserCacheService().updateCacheForPlayer(x);
        } catch (Exception e) {
            Nucleus.getNucleus().printStackTraceIfDebugMode(e);
        }
    }

    @Override public void onReload() {
        CoreConfig c = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(CoreConfigAdapter.class)
                .getNodeOrDefault();
        this.getKickOnStopMessage = c.isKickOnStop() ? c.getKickOnStopMessage() : null;
        this.warnOnWildcard = c.isCheckForWildcard();
    }

    @Listener
    public void onServerAboutToStop(final GameStoppingServerEvent event) {
        Nucleus.getNucleus().getUserDataManager().getOnlineUsers().forEach(x -> x.getPlayer().ifPresent(y -> onPlayerQuit(x, y)));

        if (this.getKickOnStopMessage != null) {
            for (Player p : Sponge.getServer().getOnlinePlayers()) {
                Text msg = this.getKickOnStopMessage.getForCommandSource(p);
                if (msg.isEmpty()) {
                    p.kick();
                } else {
                    p.kick(msg);
                }
            }
        }

    }

    @Listener
    public void onGameReload(final GameReloadEvent event) {
        CommandSource requester = event.getCause().first(CommandSource.class).orElse(Sponge.getServer().getConsole());
        if (Nucleus.getNucleus().reload()) {
            requester.sendMessage(Text.of(TextColors.YELLOW, "[Nucleus] ",
                    Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.reload.one")));
            requester.sendMessage(Text.of(TextColors.YELLOW, "[Nucleus] ",
                    Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.reload.two")));
        } else {
            requester.sendMessage(Text.of(TextColors.RED, "[Nucleus] ",
                    Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.reload.errorone")));
        }
    }
}
