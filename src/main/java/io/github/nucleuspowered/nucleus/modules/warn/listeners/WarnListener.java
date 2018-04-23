/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warn.listeners;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.warn.config.WarnConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.warn.data.WarnData;
import io.github.nucleuspowered.nucleus.modules.warn.handlers.WarnHandler;
import io.github.nucleuspowered.nucleus.util.PermissionMessageChannel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MutableMessageChannel;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class WarnListener implements Reloadable, ListenerBase {

    private final WarnHandler handler = getServiceUnchecked(WarnHandler.class);
    private final String showOnLogin = PermissionRegistry.PERMISSIONS_PREFIX + "warn.showonlogin";
    private boolean isShowOnLogin = true;

    /**
     * At the time the subject joins, check to see if the subject has been warned.
     *
     * @param event The event.
     */
    @Listener
    public void onPlayerLogin(final ClientConnectionEvent.Join event) {
        Sponge.getScheduler().createTaskBuilder().async().delay(500, TimeUnit.MILLISECONDS).execute(() -> {
            Player player = event.getTargetEntity();
            List<WarnData> warnings = this.handler.getWarningsInternal(player, true, false);
            if (warnings != null && !warnings.isEmpty()) {
                for (WarnData warning : warnings) {
                    warning.nextLoginToTimestamp();

                    if (warning.getEndTimestamp().isPresent() && warning.getEndTimestamp().get().isBefore(Instant.now())) {
                        this.handler.removeWarning(player, warning);
                    } else {
                        if (this.isShowOnLogin) {
                            if (warning.getEndTimestamp().isPresent()) {
                                player.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("warn.playernotify.time", warning.getReason(),
                                        Util.getTimeStringFromSeconds(Instant.now().until(warning.getEndTimestamp().get(), ChronoUnit.SECONDS))));
                            } else {
                                player.sendMessage(
                                        Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("warn.playernotify.standard", warning.getReason()));
                            }
                        }
                    }
                }

                // Now, let's check again
                if (this.isShowOnLogin) {
                    List<WarnData> lwd = warnings.stream().filter(x -> !x.isExpired()).collect(Collectors.toList());
                    if (!lwd.isEmpty()) {
                        MutableMessageChannel messageChannel = new PermissionMessageChannel(this.showOnLogin).asMutable();
                        messageChannel.send(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("warn.login.notify", player.getName(), String.valueOf(lwd.size())).toBuilder()
                                .onHover(TextActions.showText(
                                        Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("warn.login.view", player.getName())))
                                .onClick(TextActions.runCommand("/checkwarnings " + player.getName()))
                                .build());
                    }
                }
            }
        }).submit(Nucleus.getNucleus());
    }

    @Override
    public Map<String, PermissionInformation> getPermissions() {
        Map<String, PermissionInformation> mp = Maps.newHashMap();
        mp.put(this.showOnLogin, PermissionInformation.getWithTranslation("permission.warn.showonlogin", SuggestedLevel.MOD));
        return mp;
    }

    @Override public void onReload() {
        this.isShowOnLogin = getServiceUnchecked(WarnConfigAdapter.class).getNodeOrDefault().isShowOnLogin();
    }
}
