/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.configurate.typeserialisers;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

public class Vector3dTypeSerialiser implements TypeSerializer<Vector3d> {

    @Override
    public Vector3d deserialize(TypeToken<?> type, ConfigurationNode value) {
        return new Vector3d(value.getNode("rotx").getDouble(), value.getNode("roty").getDouble(), value.getNode("rotz").getDouble());
    }

    @Override
    public void serialize(TypeToken<?> type, Vector3d obj, ConfigurationNode value) {
        value.getNode("rotx").setValue(obj.getX());
        value.getNode("roty").setValue(obj.getY());
        value.getNode("rotz").setValue(obj.getZ());
    }
}
