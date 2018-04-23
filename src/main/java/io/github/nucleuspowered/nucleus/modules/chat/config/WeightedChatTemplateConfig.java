/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chat.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class WeightedChatTemplateConfig extends ChatTemplateConfig {

    @Setting(comment = "config.chat.weight")
    private int weight = 1;

    public int getWeight() {
        return this.weight;
    }
}
