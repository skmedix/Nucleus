/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chatlogger.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.modules.chatlogger.ChatLoggerModule;
import io.github.nucleuspowered.nucleus.modules.chatlogger.config.ChatLoggingConfig;
import io.github.nucleuspowered.nucleus.modules.chatlogger.config.ChatLoggingConfigAdapter;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;

import java.io.IOException;

public class BaseLoggerListener extends AbstractLoggerListener {

    @Listener
    public void onShutdown(GameStoppedServerEvent event) {
        try {
            this.handler.onServerShutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean shouldEnable() {
        return Nucleus.getNucleus().getConfigValue(ChatLoggerModule.ID, ChatLoggingConfigAdapter.class, ChatLoggingConfig::isEnableLog).orElse(false);
    }

}
