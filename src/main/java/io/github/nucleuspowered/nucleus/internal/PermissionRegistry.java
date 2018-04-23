/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.PluginInfo;
import io.github.nucleuspowered.nucleus.internal.annotations.command.PermissionsFrom;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;

import java.util.HashMap;
import java.util.Map;

public class PermissionRegistry {

    public final static String PERMISSIONS_PREFIX = PluginInfo.ID + ".";
    private final Map<Class<? extends AbstractCommand>, CommandPermissionHandler> serviceRegistry = Maps.newHashMap();
    private final Map<String, PermissionInformation> otherPermissions = Maps.newHashMap();

    public CommandPermissionHandler getPermissionsForNucleusCommand(Class<? extends AbstractCommand> command) {
        if (this.serviceRegistry.containsKey(command)) {
            return this.serviceRegistry.get(command);
        }

        PermissionsFrom p = command.getAnnotation(PermissionsFrom.class);
        if (p != null && p.value() != AbstractCommand.class) {
            return getPermissionsForNucleusCommand(p.value());
        }

        CommandPermissionHandler handler = new CommandPermissionHandler(command, Nucleus.getNucleus());
        this.serviceRegistry.put(command, handler);
        return handler;
    }

    public void addHandler(Class<? extends AbstractCommand> cb, CommandPermissionHandler cph) {
        if (this.serviceRegistry.containsKey(cb)) {
            // Silently discard.
            return;
        }

        this.serviceRegistry.put(cb, cph);
    }

    public void registerOtherPermission(String otherPermission, PermissionInformation pi) {
        if (this.otherPermissions.containsKey(otherPermission)) {
            // Silently discard.
            return;
        }

        this.otherPermissions.put(otherPermission, pi);
    }

    public void registerOtherPermission(String otherPermission, String description, SuggestedLevel level) {
        this.registerOtherPermission(otherPermission, new PermissionInformation(description, level));
    }

    public Map<String, PermissionInformation> getPermissions() {
        Map<String, PermissionInformation> m = new HashMap<>();
        this.serviceRegistry.values().forEach(x -> m.putAll(x.getSuggestedPermissions()));
        m.putAll(this.otherPermissions);
        return m;
    }
}
