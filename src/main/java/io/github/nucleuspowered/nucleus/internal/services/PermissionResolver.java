/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.services;

import org.spongepowered.api.service.permission.Subject;

@FunctionalInterface
public interface PermissionResolver {

    PermissionResolver SIMPLE = Subject::hasPermission;

    boolean hasPermission(Subject subject, String permission);
}
