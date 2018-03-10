/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.storage.api.events;

import io.github.nucleuspowered.nucleus.storage.api.storage.StorageService;
import org.spongepowered.api.event.Event;

public interface RegisterStorageEvent extends Event {

    boolean register(String id, StorageService service);
}
