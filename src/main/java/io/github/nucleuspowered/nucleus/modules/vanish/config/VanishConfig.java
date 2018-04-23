/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.vanish.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class VanishConfig {

    @Setting(value = "hide-connection-messages-on-vanish", comment = "config.vanish.connectionmessages")
    private boolean suppressMessagesOnVanish = false;

    //@RequiresProperty("nucleus.vanish.tablist.enable")
    @Setting(value = "alter-tab-list", comment = "config.vanish.altertablist")
    private boolean alterTabList = false;

    public boolean isSuppressMessagesOnVanish() {
        return this.suppressMessagesOnVanish;
    }

    public boolean isAlterTabList() {
        return this.alterTabList;
    }
}
