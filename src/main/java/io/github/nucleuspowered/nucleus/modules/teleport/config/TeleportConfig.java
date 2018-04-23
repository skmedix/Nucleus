/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class TeleportConfig {

    @Setting(value = "use-safe-teleportation", comment = "config.teleport.safe")
    private boolean useSafeTeleport = true;

    @Setting(value = "default-quiet", comment = "config.teleport.quiet")
    private boolean defaultQuiet = true;

    public boolean isDefaultQuiet() {
        return this.defaultQuiet;
    }

    public boolean isUseSafeTeleport() {
        return this.useSafeTeleport;
    }
}
