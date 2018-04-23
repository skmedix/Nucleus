/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class AFKConfig {

    @Setting(value = "afktime", comment = "config.afk.time")
    private long afkTime = 300;

    @Setting(value = "afktimetokick", comment = "config.afk.timetokick")
    private long afkTimeToKick = 0;

    @Setting(value = "afk-when-vanished", comment = "config.afk.whenvanished")
    private boolean afkOnVanish = false;

    @Setting(value = "alert-on-command-send", comment = "config.afk.oncommandsend")
    private boolean alertSenderOnAfk = true;

    @Setting(value = "broadcast-to-all-on-kick", comment = "config.afk.broadcastonkick")
    private boolean broadcastOnKick = true;

    @Setting(value = "messages", comment = "config.afk.messages.base")
    private MessagesConfig messages = new MessagesConfig();

    @Setting(value = "triggers", comment = "config.afk.triggers.summary")
    private Triggers triggers = new Triggers();

    public Triggers getTriggers() {
        return this.triggers;
    }

    public long getAfkTime() {
        return this.afkTime;
    }

    public long getAfkTimeToKick() {
        return this.afkTimeToKick;
    }

    public boolean isAfkOnVanish() {
        return this.afkOnVanish;
    }

    public boolean isAlertSenderOnAfk() {
        return this.alertSenderOnAfk;
    }

    public boolean isBroadcastOnKick() {
        return this.broadcastOnKick;
    }

    public MessagesConfig getMessages() {
        return this.messages;
    }

    @ConfigSerializable
    public static class Triggers {

        @Setting(value = "on-chat", comment = "config.afk.triggers.onchat")
        private boolean onChat = true;

        @Setting(value = "on-command", comment = "config.afk.triggers.oncommand")
        private boolean onCommand = true;

        @Setting(value = "on-movement", comment = "config.afk.triggers.onmove")
        private boolean onMovement = true;

        @Setting(value = "on-rotation", comment = "config.afk.triggers.onrotation")
        private boolean onRotation = true;

        @Setting(value = "on-interact", comment = "config.afk.triggers.oninteract")
        private boolean onInteract = true;

        public boolean isOnChat() {
            return this.onChat;
        }

        public boolean isOnCommand() {
            return this.onCommand;
        }

        public boolean isOnMovement() {
            return this.onMovement;
        }

        public boolean isOnRotation() {
            return this.onRotation;
        }

        public boolean isOnInteract() {
            return this.onInteract;
        }
    }
}
