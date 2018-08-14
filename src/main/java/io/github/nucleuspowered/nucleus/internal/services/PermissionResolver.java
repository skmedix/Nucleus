package io.github.nucleuspowered.nucleus.internal.services;

import org.spongepowered.api.service.permission.Subject;

@FunctionalInterface
public interface PermissionResolver {

    PermissionResolver SIMPLE = Subject::hasPermission;

    boolean hasPermission(Subject subject, String permission);
}
