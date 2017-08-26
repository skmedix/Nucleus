/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.configurate.datatypes;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.configurate.wrappers.NucleusItemStackSnapshot;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public class KitDataNode {

    public KitDataNode() {
    }

    public KitDataNode(List<NucleusItemStackSnapshot> stacks, long interval, double cost, boolean autoRedeem, boolean oneTime, boolean displayMessage,
            boolean ignoresPermission, boolean hidden, List<String> commands, boolean firstJoin) {
        this.stacks = stacks;
        this.interval = interval;
        this.cost = cost;
        this.autoRedeem = autoRedeem;
        this.oneTime = oneTime;
        this.displayMessage = displayMessage;
        this.ignoresPermission = ignoresPermission;
        this.hidden = hidden;
        this.commands = commands;
        this.firstJoin = firstJoin;
    }

    @Setting public List<NucleusItemStackSnapshot> stacks = Lists.newArrayList();

    /**
     * This is in seconds to be consistent with the rest of the plugin.
     */
    @Setting public long interval = 0;

    @Setting public double cost = 0;

    @Setting public boolean autoRedeem = false;

    @Setting public boolean oneTime = false;

    @Setting public boolean displayMessage = true;

    @Setting public boolean ignoresPermission = false;

    @Setting public boolean hidden = false;

    @Setting public List<String> commands = Lists.newArrayList();

    @Setting public boolean firstJoin = false;

}
