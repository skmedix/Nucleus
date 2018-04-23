/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chatlogger.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.modules.chatlogger.ChatLoggerModule;
import io.github.nucleuspowered.nucleus.modules.chatlogger.config.ChatLoggingConfigAdapter;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;

import java.util.Optional;

public class ChatLoggingListener extends AbstractLoggerListener {

    @Listener(order = Order.LAST)
    public void onCommand(MessageChannelEvent.Chat event) {
        Util.onSourceSimulatedOr(event, this::getSource, this::onCommand);
    }

    private void onCommand(MessageChannelEvent.Chat event, CommandSource source) {
        log(event.getMessage().toPlain(), source);
    }

    @Listener(order = Order.LAST)
    public void onCommand(SendCommandEvent event) {
        if (event.getCommand().equalsIgnoreCase("say") || event.getCommand().equalsIgnoreCase("minecraft:say")) {
            Util.onSourceSimulatedOr(event, this::getSource, this::onCommand);
        }
    }

    private void onCommand(SendCommandEvent event, CommandSource source) {
        log(event.getArguments(), source);
    }

    private void log(String s, CommandSource source) {
        String message = Nucleus.getNucleus().getMessageProvider().getMessageWithFormat("chatlog.chat", source.getName(), s);
        this.handler.queueEntry(message);
    }

    @Override public boolean shouldEnable() {
        return Nucleus.getNucleus().getConfigValue(ChatLoggerModule.ID, ChatLoggingConfigAdapter.class, x -> x.isEnableLog() && x.isLogChat())
                .orElse(false);
    }

    private Optional<CommandSource> getSource(Event event) {
        return event.getCause().first(CommandSource.class);
    }

}
