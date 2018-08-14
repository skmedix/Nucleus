package io.github.nucleuspowered.nucleus.internal.services;

import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.service.permission.Subject;

import java.util.function.Predicate;

public interface PermissionResolver {

    void registerPermissions();

    void registerPermissionPredicate(Predicate<String> perm, SuggestedLevel level);

    boolean hasPermission(Subject subject, String permission);
}
