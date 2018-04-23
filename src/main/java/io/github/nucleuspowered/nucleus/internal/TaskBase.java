/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import io.github.nucleuspowered.nucleus.annotationprocessor.Store;
import io.github.nucleuspowered.nucleus.internal.annotations.EntryPoint;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.traits.InternalServiceManagerTrait;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@EntryPoint
@NonnullByDefault
@Store(Constants.RUNNABLE)
public interface TaskBase extends Consumer<Task>, InternalServiceManagerTrait {

    boolean isAsync();

    Duration interval();

    default Map<String, PermissionInformation> getPermissions() {
        return new HashMap<>();
    }
}
