/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.service;

import java.util.Optional;
import java.util.UUID;

public interface NucleusWorldUUIDChangeService {

    Optional<UUID> getMappedUUID(UUID oldUUID);
}
