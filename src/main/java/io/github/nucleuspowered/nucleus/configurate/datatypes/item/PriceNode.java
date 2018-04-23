/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.configurate.datatypes.item;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class PriceNode {

    public PriceNode() {
        this(-1, -1);
    }

    public PriceNode(double buy, double sell) {
        this.buy = buy;
        this.sell = sell;
    }

    @Setting
    private double buy = -1;

    @Setting
    private double sell = -1;

    public double getBuy() {
        return this.buy;
    }

    public void setBuy(double buy) {
        this.buy = buy;
    }

    public double getSell() {
        return this.sell;
    }

    public void setSell(double sell) {
        this.sell = sell;
    }
}
