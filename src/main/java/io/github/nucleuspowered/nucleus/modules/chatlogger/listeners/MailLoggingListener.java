/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chatlogger.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.events.NucleusMailEvent;
import io.github.nucleuspowered.nucleus.modules.chatlogger.ChatLoggerModule;
import io.github.nucleuspowered.nucleus.modules.chatlogger.config.ChatLoggingConfigAdapter;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.First;

public class MailLoggingListener extends AbstractLoggerListener {

    @Listener(order = Order.LAST)
    public void onCommand(NucleusMailEvent event, @First CommandSource source) {
        String message = Nucleus.getNucleus().getMessageProvider().getMessageWithFormat("chatlog.mail",
            source.getName(), event.getRecipient().getName(), event.getMessage());
        this.handler.queueEntry(message);
    }

    @Override public boolean shouldEnable() {
        return Nucleus.getNucleus().getConfigValue(ChatLoggerModule.ID, ChatLoggingConfigAdapter.class, x -> x.isEnableLog() && x.isLogMail())
                .orElse(false);
    }

}
