/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import io.github.nucleuspowered.nucleus.annotationprocessor.Store;
import io.github.nucleuspowered.nucleus.internal.annotations.EntryPoint;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@EntryPoint
@NonnullByDefault
@Store(Constants.RUNNABLE)
public abstract class TaskBase implements Consumer<Task> {

    public abstract boolean isAsync();

    public abstract Duration interval();

    public Map<String, PermissionInformation> getPermissions() {
        return new HashMap<>();
    }
}
