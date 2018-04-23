/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.servershop.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ServerShopConfig {

    @Setting(value = "max-purchasable-at-once", comment = "config.servershop.maxpurchasable")
    private int maxPurchasableAtOnce = 64;

    public int getMaxPurchasableAtOnce() {
        return Math.max(this.maxPurchasableAtOnce, 1);
    }
}
