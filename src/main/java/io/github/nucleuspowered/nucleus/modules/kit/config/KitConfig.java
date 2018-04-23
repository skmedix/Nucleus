/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.config;

import io.github.nucleuspowered.neutrino.annotations.DoNotGenerate;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class KitConfig {

    @Setting(value = "must-get-all-items", comment = "config.kits.mustgetall")
    private boolean mustGetAll = false;

    @Setting(value = "drop-items-if-inventory-full", comment = "config.kits.full")
    private boolean dropKitIfFull = false;

    @DoNotGenerate
    @Setting(value = "separate-permissions", comment = "config.kits.separate")
    private boolean separatePermissions = true;

    @Setting(value = "process-tokens-in-lore", comment = "config.kits.process-tokens")
    private boolean processTokens = false;

    public boolean isMustGetAll() {
        return this.mustGetAll;
    }

    public boolean isSeparatePermissions() {
        return this.separatePermissions;
    }

    public boolean isDropKitIfFull() {
        return this.dropKitIfFull;
    }

    public boolean isProcessTokens() {
        return this.processTokens;
    }
}
