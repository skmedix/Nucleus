/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mute.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.events.NucleusMessageEvent;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.data.EndTimestamp;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.message.events.InternalNucleusHelpOpEvent;
import io.github.nucleuspowered.nucleus.modules.mute.commands.MuteCommand;
import io.github.nucleuspowered.nucleus.modules.mute.commands.VoiceCommand;
import io.github.nucleuspowered.nucleus.modules.mute.config.MuteConfig;
import io.github.nucleuspowered.nucleus.modules.mute.config.MuteConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.mute.data.MuteData;
import io.github.nucleuspowered.nucleus.modules.mute.handler.MuteHandler;
import io.github.nucleuspowered.nucleus.util.PermissionMessageChannel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class MuteListener implements Reloadable, ListenerBase {

    private final MuteHandler handler = getServiceUnchecked(MuteHandler.class);
    private MuteConfig muteConfig = new MuteConfig();
    private final String voicePerm = getPermissionHandlerFor(VoiceCommand.class).getPermissionWithSuffix("auto");

    /**
     * At the time the subject joins, check to see if the subject is muted.
     *
     * @param event The event.
     */
    @Listener
    public void onPlayerLogin(final ClientConnectionEvent.Join event) {
        // Kick off a scheduled task.
        Sponge.getScheduler().createTaskBuilder().async().delay(500, TimeUnit.MILLISECONDS).execute(() -> {
            Player user = event.getTargetEntity();
            Optional<MuteData> omd = this.handler.getPlayerMuteData(user);
            if (omd.isPresent()) {
                MuteData md = omd.get();
                md.nextLoginToTimestamp();

                if (isMuted(user)) {
                    this.handler.onMute(md, event.getTargetEntity());
                }
            }
        }).submit(Nucleus.getNucleus());
    }

    @Listener(order = Order.LATE)
    public void onChat(MessageChannelEvent.Chat event) {
        Util.onPlayerSimulatedOrPlayer(event, this::onChat);
    }

    private void onChat(MessageChannelEvent.Chat event, Player player) {
        boolean cancel = false;
        if (isMuted(player)) {
            this.handler.onMute(player);
            MessageChannel.TO_CONSOLE.send(Text.builder().append(Text.of(player.getName() + " (")).append(
                    getMessage("standard.muted")).append(Text.of("): ")).append(event.getRawMessage()).build());
            cancel = true;
        }

        if (cancelOnGlobalMute(player, false)) {
            cancel = true;
        }

        if (cancel) {
            if (this.muteConfig.isShowMutedChat()) {
                // Send it to admins only.
                String m = this.muteConfig.getCancelledTag();
                if (!m.isEmpty()) {
                    event.getFormatter().setHeader(
                        Text.join(TextSerializers.FORMATTING_CODE.deserialize(m), event.getFormatter().getHeader().toText()));
                }

                new PermissionMessageChannel(MuteCommand.getMutedChatPermission()).send(player, event.getMessage(), ChatTypes.SYSTEM);
            }

            event.setCancelled(true);
        }
    }

    @Listener
    public void onPlayerMessage(NucleusMessageEvent event, @Getter("getSender") Player source) {
        boolean isCancelled = false;
        if (isMuted(source)) {
            if (source.isOnline()) {
                this.handler.onMute(source);
            }

            isCancelled = true;
        }

        if (cancelOnGlobalMute(source, isCancelled)) {
            isCancelled = true;
        }
        event.setCancelled(isCancelled);
    }

    @Listener
    public void onPlayerHelpOp(InternalNucleusHelpOpEvent event, @Root Player source) {
        if (isMuted(source)) {
            if (source.isOnline()) {
                this.handler.onMute(source);
            }

            event.setCancelled(true);
        }

        //noinspection IsCancelled
        if (cancelOnGlobalMute(source, event.isCancelled())) {
            event.setCancelled(true);
        }
    }

    private boolean cancelOnGlobalMute(Player player, boolean isCancelled) {
        if (isCancelled || !this.handler.isGlobalMuteEnabled() || hasPermission(player, this.voicePerm)) {
            return false;
        }

        if (this.handler.isVoiced(player.getUniqueId())) {
            return false;
        }

        player.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("globalmute.novoice"));
        return true;
    }

    @Override
    public void onReload() {
        this.muteConfig = getServiceUnchecked(MuteConfigAdapter.class).getNodeOrDefault();
    }

    private boolean isMuted(Player player) {
        if (!this.handler.isMutedCached(player)) {
            return false;
        } else if (this.handler.getPlayerMuteData(player).map(EndTimestamp::expired).orElse(true)) { // true indicates expiry
            this.handler.unmutePlayer(player);
            return false;
        }

        return true;
    }
}
