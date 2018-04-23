/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.configurate.typeserialisers;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.time.Instant;

public class InstantTypeSerialiser implements TypeSerializer<Instant> {

    @Override public Instant deserialize(TypeToken<?> type, ConfigurationNode value) {
        return Instant.ofEpochMilli(value.getLong());
    }

    @Override public void serialize(TypeToken<?> type, Instant obj, ConfigurationNode value) {
        value.setValue(obj.toEpochMilli());
    }
}
