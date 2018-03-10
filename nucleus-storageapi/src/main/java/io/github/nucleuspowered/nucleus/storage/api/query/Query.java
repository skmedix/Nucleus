/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.storage.api.query;

import ninja.leaping.configurate.ConfigurationNode;

import java.util.Map;
import java.util.UUID;

public interface Query<P> {

    Map<UUID, ConfigurationNode> getResult(P parameterSet);


}
