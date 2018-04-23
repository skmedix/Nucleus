/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chatlogger.runnables;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.TaskBase;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.chatlogger.config.ChatLoggingConfig;
import io.github.nucleuspowered.nucleus.modules.chatlogger.config.ChatLoggingConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.chatlogger.handlers.ChatLoggerHandler;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@NonnullByDefault
public class ChatLoggerRunnable implements TaskBase, Reloadable {

    private final ChatLoggerHandler handler = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(ChatLoggerHandler.class);
    private ChatLoggingConfig config = new ChatLoggingConfig();

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public Duration interval() {
        return Duration.of(1, ChronoUnit.SECONDS);
    }


    @Override
    public void accept(Task task) {
        if (Sponge.getGame().getState() == GameState.SERVER_STOPPED) {
            return;
        }

        if (this.config.isEnableLog()) {
            this.handler.onTick();
        }
    }

    @Override public void onReload() {
        this.config = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(ChatLoggingConfigAdapter.class).getNodeOrDefault();
    }
}
