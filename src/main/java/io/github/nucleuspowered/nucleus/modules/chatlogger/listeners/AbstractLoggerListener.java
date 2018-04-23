/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chatlogger.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.chatlogger.handlers.ChatLoggerHandler;

public abstract class AbstractLoggerListener implements ListenerBase.Conditional {

    final ChatLoggerHandler handler = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(ChatLoggerHandler.class);

}
