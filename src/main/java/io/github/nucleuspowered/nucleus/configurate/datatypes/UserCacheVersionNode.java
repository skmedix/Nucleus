/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.configurate.datatypes;

import com.google.common.collect.Maps;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.Map;
import java.util.UUID;

@ConfigSerializable
public class UserCacheVersionNode {

    @Setting
    private int version = 1;

    @Setting
    private Map<UUID, UserCacheDataNode> node = Maps.newHashMap();

    public int getVersion() {
        return this.version;
    }

    public Map<UUID, UserCacheDataNode> getNode() {
        return this.node;
    }
}
