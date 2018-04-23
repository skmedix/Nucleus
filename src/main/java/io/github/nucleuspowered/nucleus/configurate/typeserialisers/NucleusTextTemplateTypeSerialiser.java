/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.configurate.typeserialisers;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.internal.text.NucleusTextTemplateFactory;
import io.github.nucleuspowered.nucleus.internal.text.NucleusTextTemplateImpl;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

public class NucleusTextTemplateTypeSerialiser implements TypeSerializer<NucleusTextTemplateImpl> {

    @Override public NucleusTextTemplateImpl deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        try {
            return NucleusTextTemplateFactory.createFromString(value.getString());
        } catch (Throwable throwable) {
            if (throwable instanceof ObjectMappingException) {
                throw (ObjectMappingException)throwable;
            } else {
                throw new ObjectMappingException(throwable);
            }
        }
    }

    @Override public void serialize(TypeToken<?> type, NucleusTextTemplateImpl obj, ConfigurationNode value) {
        value.setValue(obj.getRepresentation());
    }
}
