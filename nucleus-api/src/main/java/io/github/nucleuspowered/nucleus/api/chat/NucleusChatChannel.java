/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.chat;

import org.spongepowered.api.text.channel.MessageChannel;

/**
 * This interface holds chat channels that Nucleus uses.
 */
public interface NucleusChatChannel extends MessageChannel {

    /**
     * Indicates that the channel is Staff Chat.
     */
    interface StaffChat extends NucleusChatChannel, NucleusNoFormatChannel, NucleusNoIgnoreChannel {}

    /**
     * Indicates that the channel is a /me message.
     */
    interface ActionMessage extends NucleusChatChannel, NucleusNoFormatChannel {}

    /**
     * Indicates that the channel is a /helpop messages
     */
    interface HelpOp extends NucleusChatChannel, NucleusNoFormatChannel {}
}