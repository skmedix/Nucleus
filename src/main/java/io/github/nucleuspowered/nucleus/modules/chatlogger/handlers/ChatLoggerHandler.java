/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chatlogger.handlers;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.logging.AbstractLoggingHandler;
import io.github.nucleuspowered.nucleus.modules.chatlogger.config.ChatLoggingConfigAdapter;

public class ChatLoggerHandler extends AbstractLoggingHandler implements Reloadable {

    private boolean enabled = false;

    public ChatLoggerHandler() {
        super("chat", "chat");
    }

    @Override
    public void onReload() throws Exception {
        ChatLoggingConfigAdapter clca = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(ChatLoggingConfigAdapter.class);
        this.enabled = clca.getNodeOrDefault().isEnableLog();
        if (this.enabled && this.logger == null) {
            this.createLogger();
        } else if (!this.enabled && this.logger != null) {
            this.onShutdown();
        }
    }

    @Override
    protected boolean enabledLog() {
        return this.enabled;
    }
}
