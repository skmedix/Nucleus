/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.annotationprocessor.Store;
import io.github.nucleuspowered.nucleus.internal.annotations.EntryPoint;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.traits.InternalServiceManagerTrait;
import io.github.nucleuspowered.nucleus.internal.traits.MessageProviderTrait;
import io.github.nucleuspowered.nucleus.internal.traits.PermissionTrait;

import java.util.Map;

@EntryPoint
@Store(Constants.LISTENER)
public interface ListenerBase extends InternalServiceManagerTrait, PermissionTrait, MessageProviderTrait {

    default Map<String, PermissionInformation> getPermissions() {
        return Maps.newHashMap();
    }

    @EntryPoint
    @Store(Constants.LISTENER)
    interface Conditional extends ListenerBase {

        boolean shouldEnable();
    }

}
